package com.softuni.statssvc.service;

import com.softuni.statssvc.exception.StatNotFoundException;
import com.softuni.statssvc.model.dto.StatRecordRequest;
import com.softuni.statssvc.model.dto.StatResponse;
import com.softuni.statssvc.model.entity.JobStat;
import com.softuni.statssvc.repository.JobStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobStatService {

    private final JobStatRepository jobStatRepository;

    @Cacheable("stats")
    public List<StatResponse> findAll() {
        return jobStatRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value = "statByCategory", key = "#category")
    public StatResponse findByCategory(String category) {
        return jobStatRepository.findByCategory(category)
                .map(this::toResponse)
                .orElseThrow(() -> new StatNotFoundException("No stats found for category: " + category));
    }

    @Transactional
    @CacheEvict(value = {"stats", "statByCategory"}, allEntries = true)
    public StatResponse record(StatRecordRequest request) {
        log.info("Recording stat delta for category: {}", request.getCategory());
        JobStat stat = jobStatRepository.findByCategory(request.getCategory())
                .orElseGet(() -> {
                    JobStat newStat = new JobStat();
                    newStat.setCategory(request.getCategory());
                    return newStat;
                });
        stat.setTotalJobPosts(Math.max(0, stat.getTotalJobPosts() + request.getJobPostDelta()));
        stat.setTotalApplications(Math.max(0, stat.getTotalApplications() + request.getApplicationDelta()));
        stat.setLastUpdated(LocalDateTime.now());
        return toResponse(jobStatRepository.save(stat));
    }

    @Transactional
    @CacheEvict(value = {"stats", "statByCategory"}, allEntries = true)
    public StatResponse update(String category, StatRecordRequest request) {
        log.info("Updating stat for category: {}", category);
        JobStat stat = jobStatRepository.findByCategory(category)
                .orElseThrow(() -> new StatNotFoundException("No stats found for category: " + category));
        stat.setTotalJobPosts(Math.max(0, stat.getTotalJobPosts() + request.getJobPostDelta()));
        stat.setTotalApplications(Math.max(0, stat.getTotalApplications() + request.getApplicationDelta()));
        stat.setLastUpdated(LocalDateTime.now());
        return toResponse(jobStatRepository.save(stat));
    }

    @Transactional
    @CacheEvict(value = {"stats", "statByCategory"}, allEntries = true)
    public void delete(String category) {
        log.info("Deleting stat for category: {}", category);
        JobStat stat = jobStatRepository.findByCategory(category)
                .orElseThrow(() -> new StatNotFoundException("No stats found for category: " + category));
        jobStatRepository.delete(stat);
    }

    @Transactional
    @CacheEvict(value = {"stats", "statByCategory"}, allEntries = true)
    public void purgeStaleStats() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(180);
        List<JobStat> stale = jobStatRepository.findAllByLastUpdatedBefore(threshold);
        jobStatRepository.deleteAll(stale);
        log.info("Purged {} stale stat records older than 180 days", stale.size());
    }

    private StatResponse toResponse(JobStat stat) {
        return new StatResponse(stat.getId(), stat.getCategory(),
                stat.getTotalJobPosts(), stat.getTotalApplications());
    }
}
