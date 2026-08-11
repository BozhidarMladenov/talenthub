package com.softuni.talenthub.service;

import com.softuni.talenthub.client.StatsClient;
import com.softuni.talenthub.event.JobFilledEvent;
import com.softuni.talenthub.exception.ResourceNotFoundException;
import com.softuni.talenthub.exception.UnauthorizedActionException;
import com.softuni.talenthub.model.dto.JobPostRequest;
import com.softuni.talenthub.model.dto.StatRecordRequest;
import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.JobStatus;
import com.softuni.talenthub.model.enums.UserRole;
import com.softuni.talenthub.repository.ApplicationRepository;
import com.softuni.talenthub.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final ApplicationRepository applicationRepository;
    private final StatsClient statsClient;
    private final ApplicationEventPublisher eventPublisher;

    @Cacheable("openJobs")
    public List<JobPost> findAllOpen() {
        return jobPostRepository.findAllByStatus(JobStatus.OPEN);
    }

    public List<JobPost> findAllByClient(User client) {
        return jobPostRepository.findAllByClient(client);
    }

    public JobPost findById(UUID id) {
        return jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = "openJobs", allEntries = true)
    public JobPost create(JobPostRequest request, User client) {
        requireClientRole(client);
        log.info("Client {} creating job post: {}", client.getUsername(), request.getTitle());

        JobPost post = new JobPost();
        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setCategory(request.getCategory());
        post.setBudget(request.getBudget());
        post.setStatus(JobStatus.OPEN);
        post.setClient(client);
        JobPost saved = jobPostRepository.save(post);

        notifyStats(request.getCategory().name(), 1, 0);
        return saved;
    }

    @Transactional
    @CacheEvict(value = "openJobs", allEntries = true)
    public JobPost update(UUID id, JobPostRequest request, User currentUser) {
        JobPost post = findById(id);
        requireOwnership(post, currentUser);
        log.info("Updating job post {} by client {}", id, currentUser.getUsername());

        post.setTitle(request.getTitle());
        post.setDescription(request.getDescription());
        post.setCategory(request.getCategory());
        post.setBudget(request.getBudget());
        return jobPostRepository.save(post);
    }

    @Transactional
    @CacheEvict(value = "openJobs", allEntries = true)
    public void delete(UUID id, User currentUser) {
        JobPost post = findById(id);
        requireOwnership(post, currentUser);
        log.info("Deleting job post {} by client {}", id, currentUser.getUsername());

        String category = post.getCategory().name();
        long remainingInCategory = jobPostRepository.countByCategory(post.getCategory());

        List<com.softuni.talenthub.model.entity.Application> applications =
                applicationRepository.findAllByJobPost(post);
        int applicationCount = applications.size();
        applicationRepository.deleteAll(applications);
        jobPostRepository.delete(post);

        // If this was the last job post in the category, remove the stat row entirely.
        // Otherwise decrement by 1 so the aggregated totals remain accurate.
        if (remainingInCategory <= 1) {
            statsClient.deleteStat(category);
        } else {
            statsClient.updateStat(category,
                    new StatRecordRequest(category, -1, -applicationCount));
        }
    }

    @Transactional
    @CacheEvict(value = "openJobs", allEntries = true)
    public JobPost changeStatus(UUID id, JobStatus newStatus, User currentUser) {
        JobPost post = findById(id);
        requireOwnership(post, currentUser);
        log.info("Changing status of job post {} to {} by client {}", id, newStatus, currentUser.getUsername());
        post.setStatus(newStatus);
        JobPost saved = jobPostRepository.save(post);

        if (newStatus == JobStatus.FILLED) {
            eventPublisher.publishEvent(new JobFilledEvent(this, saved));
        }
        return saved;
    }

    private void notifyStats(String category, int jobDelta, int appDelta) {
        statsClient.recordStat(new StatRecordRequest(category, jobDelta, appDelta));
    }

    private void requireClientRole(User user) {
        if (user.getRole() != UserRole.CLIENT) {
            throw new UnauthorizedActionException("Only clients can manage job posts.");
        }
    }

    private void requireOwnership(JobPost post, User user) {
        if (!post.getClient().getId().equals(user.getId())) {
            throw new UnauthorizedActionException("You can only manage your own job posts.");
        }
    }
}
