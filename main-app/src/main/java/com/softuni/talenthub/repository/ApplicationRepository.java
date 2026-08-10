package com.softuni.talenthub.repository;

import com.softuni.talenthub.model.entity.Application;
import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findAllByFreelancer(User freelancer);

    List<Application> findAllByJobPost(JobPost jobPost);

    boolean existsByJobPostAndFreelancer(JobPost jobPost, User freelancer);

    Optional<Application> findByJobPostAndFreelancer(JobPost jobPost, User freelancer);

    boolean existsByFreelancerAndJobPost_ClientAndStatus(
            User freelancer, User client, com.softuni.talenthub.model.enums.ApplicationStatus status);
}
