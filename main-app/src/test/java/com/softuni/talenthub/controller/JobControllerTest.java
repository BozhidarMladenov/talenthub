package com.softuni.talenthub.controller;

import com.softuni.talenthub.config.AppUserDetailsService;
import com.softuni.talenthub.config.AppUserPrincipal;
import com.softuni.talenthub.config.JwtService;
import com.softuni.talenthub.model.entity.JobPost;
import com.softuni.talenthub.model.entity.User;
import com.softuni.talenthub.model.enums.JobCategory;
import com.softuni.talenthub.model.enums.JobStatus;
import com.softuni.talenthub.model.enums.UserRole;
import com.softuni.talenthub.service.ApplicationService;
import com.softuni.talenthub.service.CurrencyService;
import com.softuni.talenthub.service.JobPostService;
import com.softuni.talenthub.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@ActiveProfiles("test")
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobPostService jobPostService;
    @MockBean
    private ApplicationService applicationService;
    @MockBean
    private StatsService statsService;
    @MockBean
    private CurrencyService currencyService;
    @MockBean
    private AppUserDetailsService appUserDetailsService;
    @MockBean
    private JwtService jwtService;

    private User clientUser;
    private AppUserPrincipal clientPrincipal;
    private JobPost jobPost;

    @BeforeEach
    void setUp() {
        clientUser = new User();
        clientUser.setId(UUID.randomUUID());
        clientUser.setUsername("client1");
        clientUser.setFullName("Test Client");
        clientUser.setPasswordHash("hashed");
        clientUser.setRole(UserRole.CLIENT);

        clientPrincipal = new AppUserPrincipal(clientUser);

        jobPost = new JobPost();
        jobPost.setId(UUID.randomUUID());
        jobPost.setTitle("Build a REST API");
        jobPost.setDescription("Spring Boot project needing a skilled developer.");
        jobPost.setCategory(JobCategory.WEB_DEVELOPMENT);
        jobPost.setBudget(new BigDecimal("500.00"));
        jobPost.setStatus(JobStatus.OPEN);
        jobPost.setClient(clientUser);
        jobPost.setCreatedAt(LocalDateTime.now());

        Map<String, String> currencies = new LinkedHashMap<>();
        currencies.put("USD", "$500.00");
        currencies.put("EUR", "€460.00");
        when(currencyService.convertBudget(any())).thenReturn(currencies);
    }

    @Test
    void list_returnsJobListPage() throws Exception {
        when(jobPostService.findAllOpen()).thenReturn(List.of(jobPost));
        when(statsService.getAllStats()).thenReturn(List.of());

        mockMvc.perform(get("/jobs")
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk())
                .andExpect(view().name("job/list"))
                .andExpect(model().attributeExists("jobs"));
    }

    @Test
    void details_returnsDetailsPageWithCurrencies() throws Exception {
        when(jobPostService.findById(jobPost.getId())).thenReturn(jobPost);
        when(applicationService.findAllByJobPost(any())).thenReturn(List.of());

        mockMvc.perform(get("/jobs/" + jobPost.getId())
                        .with(SecurityMockMvcRequestPostProcessors.anonymous()))
                .andExpect(status().isOk())
                .andExpect(view().name("job/details"))
                .andExpect(model().attributeExists("job"))
                .andExpect(model().attributeExists("budgetInCurrencies"));
    }

    @Test
    void create_redirectsAfterSuccessfulPost() throws Exception {
        when(jobPostService.create(any(), any())).thenReturn(jobPost);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                clientPrincipal, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        mockMvc.perform(post("/jobs/create")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("title", "Build a REST API")
                        .param("description", "Spring Boot project needing a skilled developer.")
                        .param("category", "WEB_DEVELOPMENT")
                        .param("budget", "500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/jobs/*"));
    }

    @Test
    void create_showsFormAgainOnValidationError() throws Exception {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                clientPrincipal, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));

        mockMvc.perform(post("/jobs/create")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .param("title", "")
                        .param("description", "")
                        .param("budget", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("job/create"));
    }
}
