package com.softuni.statssvc.repository;

import com.softuni.statssvc.model.entity.JobStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobStatRepository extends JpaRepository<JobStat, UUID> {

    Optional<JobStat> findByCategory(String category);

    List<JobStat> findAllByLastUpdatedBefore(LocalDateTime threshold);
}
