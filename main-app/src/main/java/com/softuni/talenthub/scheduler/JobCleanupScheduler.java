package com.softuni.talenthub.scheduler;

import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.enums.JobStatus;
import com.softuni.talenthub.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobCleanupScheduler {

    private final JobPostRepository jobPostRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    @CacheEvict(value = "openJobs", allEntries = true)
    public void closeStaleJobPosts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(90);
        List<JobPost> stale = jobPostRepository.findAllByCreatedAtBefore(threshold)
                .stream()
                .filter(j -> j.getStatus() == JobStatus.OPEN)
                .toList();

        stale.forEach(j -> j.setStatus(JobStatus.CLOSED));
        jobPostRepository.saveAll(stale);

        log.info("Job cleanup: closed {} stale job posts older than 90 days", stale.size());
    }
}
