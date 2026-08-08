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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User client;
    private User freelancer;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(UUID.randomUUID());
        client.setUsername("client1");
        client.setRole(UserRole.CLIENT);

        freelancer = new User();
        freelancer.setId(UUID.randomUUID());
        freelancer.setUsername("freelancer1");
        freelancer.setRole(UserRole.FREELANCER);
    }

    @Test
    void create_savesReview() {
        when(userRepository.findById(freelancer.getId())).thenReturn(Optional.of(freelancer));
        when(reviewRepository.existsByFreelancerAndClient(freelancer, client)).thenReturn(false);

        Review saved = new Review();
        saved.setId(UUID.randomUUID());
        saved.setRating(5);
        saved.setFreelancer(freelancer);
        saved.setClient(client);
        when(reviewRepository.save(any())).thenReturn(saved);

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Excellent work!");

        Review result = reviewService.create(freelancer.getId(), request, client);

        assertThat(result.getRating()).isEqualTo(5);
        verify(reviewRepository).save(any());
    }

    @Test
    void create_throwsOnDuplicateReview() {
        when(userRepository.findById(freelancer.getId())).thenReturn(Optional.of(freelancer));
        when(reviewRepository.existsByFreelancerAndClient(freelancer, client)).thenReturn(true);

        ReviewRequest request = new ReviewRequest();
        request.setRating(4);

        assertThatThrownBy(() -> reviewService.create(freelancer.getId(), request, client))
                .isInstanceOf(InvalidOperationException.class);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void create_throwsWhenReviewingNonFreelancer() {
        User anotherClient = new User();
        anotherClient.setId(UUID.randomUUID());
        anotherClient.setRole(UserRole.CLIENT);

        when(userRepository.findById(anotherClient.getId())).thenReturn(Optional.of(anotherClient));

        ReviewRequest request = new ReviewRequest();
        request.setRating(3);

        assertThatThrownBy(() -> reviewService.create(anotherClient.getId(), request, client))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void create_throwsWhenFreelancerTriesToReview() {
        ReviewRequest request = new ReviewRequest();
        request.setRating(3);

        assertThatThrownBy(() -> reviewService.create(client.getId(), request, freelancer))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void findById_throwsWhenNotFound() {
        UUID unknown = UUID.randomUUID();
        when(reviewRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.findById(unknown))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesReview() {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setClient(client);
        review.setFreelancer(freelancer);
        review.setRating(5);
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        reviewService.delete(review.getId(), client);

        verify(reviewRepository).delete(review);
    }

    @Test
    void delete_throwsWhenNotOwner() {
        Review review = new Review();
        review.setId(UUID.randomUUID());
        review.setClient(client);
        review.setFreelancer(freelancer);
        review.setRating(5);
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        User stranger = new User();
        stranger.setId(UUID.randomUUID());
        stranger.setRole(UserRole.CLIENT);

        assertThatThrownBy(() -> reviewService.delete(review.getId(), stranger))
                .isInstanceOf(UnauthorizedActionException.class);
        verify(reviewRepository, never()).delete(any());
    }
}
