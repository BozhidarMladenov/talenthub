package com.softuni.statssvc.service;

import com.softuni.statssvc.exception.StatNotFoundException;
import com.softuni.statssvc.model.dto.StatRecordRequest;
import com.softuni.statssvc.model.dto.StatResponse;
import com.softuni.statssvc.model.entity.JobStat;
import com.softuni.statssvc.repository.JobStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobStatServiceTest {

    @Mock
    private JobStatRepository jobStatRepository;

    @InjectMocks
    private JobStatService jobStatService;

    @Test
    void record_createsNewStatWhenCategoryIsNew() {
        StatRecordRequest request = new StatRecordRequest();
        request.setCategory("DESIGN");
        request.setJobPostDelta(1);
        request.setApplicationDelta(0);

        when(jobStatRepository.findByCategory("DESIGN")).thenReturn(Optional.empty());
        JobStat saved = new JobStat();
        saved.setId(UUID.randomUUID());
        saved.setCategory("DESIGN");
        saved.setTotalJobPosts(1);
        saved.setTotalApplications(0);
        saved.setLastUpdated(LocalDateTime.now());
        when(jobStatRepository.save(any())).thenReturn(saved);

        StatResponse result = jobStatService.record(request);

        assertThat(result.getCategory()).isEqualTo("DESIGN");
        assertThat(result.getTotalJobPosts()).isEqualTo(1);
    }

    @Test
    void findByCategory_throwsWhenNotFound() {
        when(jobStatRepository.findByCategory("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobStatService.findByCategory("UNKNOWN"))
                .isInstanceOf(StatNotFoundException.class);
    }

    @Test
    void record_accumulatesDeltasOnExistingRecord() {
        JobStat existing = new JobStat();
        existing.setId(UUID.randomUUID());
        existing.setCategory("WRITING");
        existing.setTotalJobPosts(5);
        existing.setTotalApplications(10);
        existing.setLastUpdated(LocalDateTime.now());

        when(jobStatRepository.findByCategory("WRITING")).thenReturn(Optional.of(existing));
        when(jobStatRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StatRecordRequest request = new StatRecordRequest();
        request.setCategory("WRITING");
        request.setJobPostDelta(2);
        request.setApplicationDelta(5);

        StatResponse result = jobStatService.record(request);

        assertThat(result.getTotalJobPosts()).isEqualTo(7);
        assertThat(result.getTotalApplications()).isEqualTo(15);
    }
}
