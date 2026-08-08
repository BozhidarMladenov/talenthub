package com.softuni.talenthub.service;

import com.softuni.talenthub.client.StatsClient;
import com.softuni.talenthub.exception.ResourceNotFoundException;
import com.softuni.talenthub.exception.UnauthorizedActionException;
import com.softuni.talenthub.model.dto.JobPostRequest;
import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.JobCategory;
import com.softuni.talenthub.model.enums.JobStatus;
import com.softuni.talenthub.model.enums.UserRole;
import com.softuni.talenthub.repository.ApplicationRepository;
import com.softuni.talenthub.repository.JobPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobPostServiceTest {

    @Mock
    private JobPostRepository jobPostRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private StatsClient statsClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private JobPostService jobPostService;

    private User client;
    private User freelancer;
    private JobPost jobPost;

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

        jobPost = new JobPost();
        jobPost.setId(UUID.randomUUID());
        jobPost.setTitle("Build a REST API");
        jobPost.setCategory(JobCategory.WEB_DEVELOPMENT);
        jobPost.setStatus(JobStatus.OPEN);
        jobPost.setClient(client);
        jobPost.setBudget(new BigDecimal("500.00"));
    }

    @Test
    void create_savesJobPostForClient() {
        JobPostRequest request = new JobPostRequest();
        request.setTitle("Build a REST API");
        request.setDescription("Need a Spring Boot developer for a REST project.");
        request.setCategory(JobCategory.WEB_DEVELOPMENT);
        request.setBudget(new BigDecimal("500.00"));
        when(jobPostRepository.save(any())).thenReturn(jobPost);

        JobPost result = jobPostService.create(request, client);

        assertThat(result.getTitle()).isEqualTo("Build a REST API");
        assertThat(result.getStatus()).isEqualTo(JobStatus.OPEN);
        verify(statsClient).recordStat(any());
    }

    @Test
    void create_throwsWhenFreelancerTriesToPost() {
        JobPostRequest request = new JobPostRequest();
        request.setTitle("Build something");
        request.setDescription("Description here with enough characters.");
        request.setCategory(JobCategory.DESIGN);
        request.setBudget(new BigDecimal("100.00"));

        assertThatThrownBy(() -> jobPostService.create(request, freelancer))
                .isInstanceOf(UnauthorizedActionException.class);
        verifyNoInteractions(jobPostRepository);
    }

    @Test
    void findById_returnsJobPost() {
        when(jobPostRepository.findById(jobPost.getId())).thenReturn(Optional.of(jobPost));

        JobPost result = jobPostService.findById(jobPost.getId());

        assertThat(result.getId()).isEqualTo(jobPost.getId());
    }

    @Test
    void findById_throwsWhenNotFound() {
        UUID unknown = UUID.randomUUID();
        when(jobPostRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostService.findById(unknown))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllOpen_returnsOpenJobs() {
        when(jobPostRepository.findAllByStatus(JobStatus.OPEN)).thenReturn(List.of(jobPost));

        List<JobPost> result = jobPostService.findAllOpen();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    void update_savesChanges() {
        when(jobPostRepository.findById(jobPost.getId())).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobPostRequest request = new JobPostRequest();
        request.setTitle("Updated Title");
        request.setDescription("Updated description with enough characters to pass validation.");
        request.setCategory(JobCategory.DESIGN);
        request.setBudget(new BigDecimal("750.00"));

        JobPost result = jobPostService.update(jobPost.getId(), request, client);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getCategory()).isEqualTo(JobCategory.DESIGN);
    }

    @Test
    void update_throwsWhenNotOwner() {
        when(jobPostRepository.findById(jobPost.getId())).thenReturn(Optional.of(jobPost));
        User otherClient = new User();
        otherClient.setId(UUID.randomUUID());
        otherClient.setRole(UserRole.CLIENT);

        JobPostRequest request = new JobPostRequest();
        request.setTitle("Hijacked");
        request.setDescription("Some valid description text here.");
        request.setCategory(JobCategory.DESIGN);
        request.setBudget(new BigDecimal("100.00"));

        assertThatThrownBy(() -> jobPostService.update(jobPost.getId(), request, otherClient))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void changeStatus_closesOpenJob() {
        when(jobPostRepository.findById(jobPost.getId())).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobPost result = jobPostService.changeStatus(jobPost.getId(), JobStatus.CLOSED, client);

        assertThat(result.getStatus()).isEqualTo(JobStatus.CLOSED);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void changeStatus_filledPublishesEvent() {
        when(jobPostRepository.findById(jobPost.getId())).thenReturn(Optional.of(jobPost));
        when(jobPostRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        JobPost result = jobPostService.changeStatus(jobPost.getId(), JobStatus.FILLED, client);

        assertThat(result.getStatus()).isEqualTo(JobStatus.FILLED);
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void delete_deletesJobAndNotifiesStats() {
        when(jobPostRepository.findById(jobPost.getId())).thenReturn(Optional.of(jobPost));
        when(applicationRepository.findAllByJobPost(jobPost)).thenReturn(List.of());

        jobPostService.delete(jobPost.getId(), client);

        verify(jobPostRepository).delete(jobPost);
        verify(statsClient).deleteStat(anyString());
    }

    @Test
    void delete_throwsWhenNotOwner() {
        when(jobPostRepository.findById(jobPost.getId())).thenReturn(Optional.of(jobPost));
        User otherClient = new User();
        otherClient.setId(UUID.randomUUID());
        otherClient.setRole(UserRole.CLIENT);

        assertThatThrownBy(() -> jobPostService.delete(jobPost.getId(), otherClient))
                .isInstanceOf(UnauthorizedActionException.class);
        verify(jobPostRepository, never()).delete(any());
    }
}
