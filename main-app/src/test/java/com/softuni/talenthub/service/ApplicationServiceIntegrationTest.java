package com.softuni.talenthub.service;

import com.softuni.talenthub.client.StatsClient;
import com.softuni.talenthub.exception.InvalidOperationException;
import com.softuni.talenthub.exception.UnauthorizedActionException;
import com.softuni.talenthub.model.dto.ApplicationRequest;
import com.softuni.talenthub.model.dto.JobPostRequest;
import com.softuni.talenthub.model.dto.RegisterRequest;
import com.softuni.talenthub.model.dto.StatResponse;
import com.softuni.talenthub.model.entity.Application;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.ApplicationStatus;
import com.softuni.talenthub.model.enums.JobCategory;
import com.softuni.talenthub.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ApplicationServiceIntegrationTest {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private JobPostService jobPostService;
    @Autowired
    private UserService userService;

    @MockBean
    private StatsClient statsClient;

    private User client;
    private User freelancer;
    private UUID jobPostId;

    @BeforeEach
    void setUp() {
        when(statsClient.recordStat(any())).thenReturn(new StatResponse());
        when(statsClient.updateStat(anyString(), any())).thenReturn(new StatResponse());

        RegisterRequest clientReq = new RegisterRequest();
        clientReq.setUsername("client_intg");
        clientReq.setEmail("client_intg@test.com");
        clientReq.setFullName("Client Integration");
        clientReq.setPassword("password123");
        clientReq.setConfirmPassword("password123");
        clientReq.setRole(UserRole.CLIENT);
        client = userService.register(clientReq);

        RegisterRequest freeReq = new RegisterRequest();
        freeReq.setUsername("free_intg");
        freeReq.setEmail("free_intg@test.com");
        freeReq.setFullName("Freelancer Integration");
        freeReq.setPassword("password123");
        freeReq.setConfirmPassword("password123");
        freeReq.setRole(UserRole.FREELANCER);
        freelancer = userService.register(freeReq);

        JobPostRequest jobReq = new JobPostRequest();
        jobReq.setTitle("Integration test job");
        jobReq.setDescription("A job post used in the integration test suite.");
        jobReq.setCategory(JobCategory.WEB_DEVELOPMENT);
        jobReq.setBudget(new BigDecimal("300.00"));
        jobPostId = jobPostService.create(jobReq, client).getId();
    }

    @Test
    void apply_createsPendingApplication() {
        ApplicationRequest req = buildApplicationRequest();

        Application result = applicationService.apply(jobPostId, req, freelancer);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(result.getFreelancer().getId()).isEqualTo(freelancer.getId());
    }

    @Test
    void apply_throwsOnDuplicateApplication() {
        ApplicationRequest req = buildApplicationRequest();
        applicationService.apply(jobPostId, req, freelancer);

        assertThatThrownBy(() -> applicationService.apply(jobPostId, req, freelancer))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void apply_throwsWhenClientTriesToApply() {
        ApplicationRequest req = buildApplicationRequest();

        assertThatThrownBy(() -> applicationService.apply(jobPostId, req, client))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void withdraw_changesStatusToWithdrawn() {
        Application application = applicationService.apply(jobPostId, buildApplicationRequest(), freelancer);

        applicationService.withdraw(application.getId(), freelancer);

        assertThat(applicationService.findById(application.getId()).getStatus())
                .isEqualTo(ApplicationStatus.WITHDRAWN);
    }

    @Test
    void withdraw_throwsWhenNotOwner() {
        Application application = applicationService.apply(jobPostId, buildApplicationRequest(), freelancer);

        assertThatThrownBy(() -> applicationService.withdraw(application.getId(), client))
                .isInstanceOf(UnauthorizedActionException.class);
    }

    @Test
    void decide_clientCanAcceptPendingApplication() {
        Application application = applicationService.apply(jobPostId, buildApplicationRequest(), freelancer);

        Application result = applicationService.decide(application.getId(), ApplicationStatus.ACCEPTED, client);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.ACCEPTED);
    }

    @Test
    void decide_clientCanRejectPendingApplication() {
        Application application = applicationService.apply(jobPostId, buildApplicationRequest(), freelancer);

        Application result = applicationService.decide(application.getId(), ApplicationStatus.REJECTED, client);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void decide_throwsWhenAlreadyDecided() {
        Application application = applicationService.apply(jobPostId, buildApplicationRequest(), freelancer);
        applicationService.decide(application.getId(), ApplicationStatus.ACCEPTED, client);

        assertThatThrownBy(() -> applicationService.decide(application.getId(), ApplicationStatus.REJECTED, client))
                .isInstanceOf(InvalidOperationException.class);
    }

    private ApplicationRequest buildApplicationRequest() {
        ApplicationRequest req = new ApplicationRequest();
        req.setCoverLetter("I am very experienced with Spring Boot and REST APIs.");
        req.setProposedRate(new BigDecimal("250.00"));
        return req;
    }
}

