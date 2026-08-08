package com.softuni.talenthub.repository;

import com.softuni.talenthub.model.entity.Review;
import com.softuni.talenthub.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findAllByFreelancer(User freelancer);

    boolean existsByFreelancerAndClient(User freelancer, User client);
}
