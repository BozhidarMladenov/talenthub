package com.softuni.talenthub.service;

import com.softuni.talenthub.client.StatsClient;
import com.softuni.talenthub.exception.InvalidOperationException;
import com.softuni.talenthub.exception.ResourceNotFoundException;
import com.softuni.talenthub.exception.UnauthorizedActionException;
import com.softuni.talenthub.model.dto.ApplicationRequest;
import com.softuni.talenthub.model.dto.StatRecordRequest;
import com.softuni.talenthub.model.entity.Application;
import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.ApplicationStatus;
import com.softuni.talenthub.model.enums.JobStatus;
import com.softuni.talenthub.model.enums.UserRole;
import com.softuni.talenthub.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobPostService jobPostService;
    private final StatsClient statsClient;

    public List<Application> findAllByFreelancer(User freelancer) {
        return applicationRepository.findAllByFreelancer(freelancer);
    }

    public List<Application> findAllByJobPost(JobPost jobPost) {
        return applicationRepository.findAllByJobPost(jobPost);
    }

    public Application findById(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    @Transactional
    public Application apply(UUID jobPostId, ApplicationRequest request, User freelancer) {
        if (freelancer.getRole() != UserRole.FREELANCER) {
            throw new UnauthorizedActionException("Only freelancers can apply to job posts.");
        }
        JobPost jobPost = jobPostService.findById(jobPostId);
        if (jobPost.getStatus() != JobStatus.OPEN) {
            throw new InvalidOperationException("This job post is no longer accepting applications.");
        }
        if (applicationRepository.existsByJobPostAndFreelancer(jobPost, freelancer)) {
            throw new InvalidOperationException("You have already applied to this job post.");
        }
        log.info("Freelancer {} applying to job post {}", freelancer.getUsername(), jobPostId);

        Application application = new Application();
        application.setJobPost(jobPost);
        application.setFreelancer(freelancer);
        application.setCoverLetter(request.getCoverLetter());
        application.setProposedRate(request.getProposedRate());
        application.setStatus(ApplicationStatus.PENDING);
        Application saved = applicationRepository.save(application);

        notifyStats(jobPost.getCategory().name(), 0, 1);
        return saved;
    }

    @Transactional
    public Application decide(UUID id, ApplicationStatus decision, User currentUser) {
        Application application = findById(id);
        if (!application.getJobPost().getClient().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("Only the job poster can decide on applications.");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidOperationException("Only pending applications can be decided.");
        }
        log.info("Client {} setting application {} to {}", currentUser.getUsername(), id, decision);
        application.setStatus(decision);
        return applicationRepository.save(application);
    }

    @Transactional
    public void withdraw(UUID id, User currentUser) {
        Application application = findById(id);
        if (!application.getFreelancer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedActionException("You can only withdraw your own applications.");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new InvalidOperationException("Only pending applications can be withdrawn.");
        }
        log.info("Freelancer {} withdrawing application {}", currentUser.getUsername(), id);
        application.setStatus(ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    private void notifyStats(String category, int jobDelta, int appDelta) {
        statsClient.updateStat(category, new StatRecordRequest(category, jobDelta, appDelta));
    }
}
