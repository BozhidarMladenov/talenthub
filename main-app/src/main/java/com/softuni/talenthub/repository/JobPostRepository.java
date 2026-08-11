package com.softuni.talenthub.repository;

import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.JobCategory;
import com.softuni.talenthub.model.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface JobPostRepository extends JpaRepository<JobPost, UUID> {

    List<JobPost> findAllByStatus(JobStatus status);

    List<JobPost> findAllByClient(User client);

    List<JobPost> findAllByStatusAndCategory(JobStatus status, JobCategory category);

    List<JobPost> findAllByCreatedAtBefore(LocalDateTime threshold);

    List<JobPost> findAllByCreatedAtBeforeAndStatus(LocalDateTime threshold, JobStatus status);

    long countByCategory(JobCategory category);
}
