package com.softuni.statssvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softuni.statssvc.model.dto.StatRecordRequest;
import com.softuni.statssvc.model.dto.StatResponse;
import com.softuni.statssvc.service.JobStatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
@ActiveProfiles("test")
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobStatService jobStatService;

    @Test
    void getAll_returnsHateoasCollectionWithOk() throws Exception {
        StatResponse stat = new StatResponse(UUID.randomUUID(), "WEB_DEVELOPMENT", 10, 25);
        when(jobStatService.findAll()).thenReturn(List.of(stat));

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.statResponseList[0].category").value("WEB_DEVELOPMENT"))
                .andExpect(jsonPath("$._embedded.statResponseList[0].totalJobPosts").value(10))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void getByCategory_returnsHateoasEntityWithOk() throws Exception {
        StatResponse stat = new StatResponse(UUID.randomUUID(), "DESIGN", 5, 12);
        when(jobStatService.findByCategory("DESIGN")).thenReturn(stat);

        mockMvc.perform(get("/api/stats/DESIGN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("DESIGN"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links['all-stats']").exists());
    }

    @Test
    void record_returnsHateoasEntityWithCreated() throws Exception {
        StatRecordRequest request = new StatRecordRequest();
        request.setCategory("DESIGN");
        request.setJobPostDelta(1);
        request.setApplicationDelta(0);

        StatResponse response = new StatResponse(UUID.randomUUID(), "DESIGN", 1, 0);
        when(jobStatService.record(any())).thenReturn(response);

        mockMvc.perform(post("/api/stats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("DESIGN"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void update_returnsHateoasEntityWithOk() throws Exception {
        StatRecordRequest request = new StatRecordRequest();
        request.setCategory("DESIGN");
        request.setApplicationDelta(3);

        StatResponse response = new StatResponse(UUID.randomUUID(), "DESIGN", 1, 3);
        when(jobStatService.update(anyString(), any())).thenReturn(response);

        mockMvc.perform(put("/api/stats/DESIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(3));
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/stats/DESIGN"))
                .andExpect(status().isNoContent());
    }
}
