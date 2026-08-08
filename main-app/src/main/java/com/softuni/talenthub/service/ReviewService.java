package com.softuni.talenthub.service;

import com.softuni.talenthub.exception.InvalidOperationException;
import com.softuni.talenthub.exception.ResourceNotFoundException;
import com.softuni.talenthub.exception.UnauthorizedActionException;
import com.softuni.talenthub.model.dto.ReviewRequest;
import com.softuni.talenthub.model.entity.Review;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.UserRole;
import com.softuni.talenthub.repository.ReviewRepository;
import com.softuni.talenthub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public List<Review> findAllForFreelancer(User freelancer) {
        return reviewRepository.findAllByFreelancer(freelancer);
    }

    public Review findById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
    }

    @Transactional
    public Review create(UUID freelancerId, ReviewRequest request, User client) {
        if (client.getRole() != UserRole.CLIENT) {
            throw new UnauthorizedActionException("Only clients can leave reviews.");
        }
        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found with id: " + freelancerId));
        if (freelancer.getRole() != UserRole.FREELANCER) {
            throw new InvalidOperationException("Reviews can only be left for freelancers.");
        }
        if (reviewRepository.existsByFreelancerAndClient(freelancer, client)) {
            throw new InvalidOperationException("You have already reviewed this freelancer.");
        }
        log.info("Client {} leaving review for freelancer {}", client.getUsername(), freelancer.getUsername());

        Review review = new Review();
        review.setFreelancer(freelancer);
        review.setClient(client);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        return reviewRepository.save(review);
    }

    @Transactional
    public void delete(UUID id, User currentUser) {
        Review review = findById(id);
        if (!review.getClient().getId().equals(currentUser.getId())
                && currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedActionException("You can only delete your own reviews.");
        }
        log.info("Deleting review {} by user {}", id, currentUser.getUsername());
        reviewRepository.delete(review);
    }
}
