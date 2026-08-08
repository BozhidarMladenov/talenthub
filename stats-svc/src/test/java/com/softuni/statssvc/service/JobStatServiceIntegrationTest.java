package com.softuni.statssvc.service;

import com.softuni.statssvc.exception.StatNotFoundException;
import com.softuni.statssvc.model.dto.StatRecordRequest;
import com.softuni.statssvc.model.dto.StatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class JobStatServiceIntegrationTest {

    @Autowired
    private JobStatService jobStatService;

    @Test
    void record_persistsNewStatToDatabase() {
        StatRecordRequest request = new StatRecordRequest();
        request.setCategory("DESIGN");
        request.setJobPostDelta(3);
        request.setApplicationDelta(7);

        StatResponse result = jobStatService.record(request);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getCategory()).isEqualTo("DESIGN");
        assertThat(result.getTotalJobPosts()).isEqualTo(3);
        assertThat(result.getTotalApplications()).isEqualTo(7);
    }

    @Test
    void record_thenFindByCategory_returnsPersistedValues() {
        StatRecordRequest request = new StatRecordRequest();
        request.setCategory("MARKETING");
        request.setJobPostDelta(2);
        request.setApplicationDelta(5);
        jobStatService.record(request);

        StatResponse found = jobStatService.findByCategory("MARKETING");

        assertThat(found.getTotalJobPosts()).isEqualTo(2);
        assertThat(found.getTotalApplications()).isEqualTo(5);
        assertThat(found.getAverageApplicationsPerJob()).isEqualTo(2.5);
    }

    @Test
    void record_twice_accumulatesDeltas() {
        StatRecordRequest first = new StatRecordRequest();
        first.setCategory("DEVOPS");
        first.setJobPostDelta(1);
        first.setApplicationDelta(0);
        jobStatService.record(first);

        StatRecordRequest second = new StatRecordRequest();
        second.setCategory("DEVOPS");
        second.setJobPostDelta(0);
        second.setApplicationDelta(4);
        jobStatService.record(second);

        StatResponse result = jobStatService.findByCategory("DEVOPS");
        assertThat(result.getTotalJobPosts()).isEqualTo(1);
        assertThat(result.getTotalApplications()).isEqualTo(4);
    }

    @Test
    void delete_removesStatFromDatabase() {
        StatRecordRequest request = new StatRecordRequest();
        request.setCategory("WRITING");
        request.setJobPostDelta(1);
        request.setApplicationDelta(2);
        jobStatService.record(request);

        jobStatService.delete("WRITING");

        assertThatThrownBy(() -> jobStatService.findByCategory("WRITING"))
                .isInstanceOf(StatNotFoundException.class);
    }
}
