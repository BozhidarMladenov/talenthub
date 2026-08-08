package com.softuni.statssvc.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatResponse {

    private UUID id;
    private String category;
    private int totalJobPosts;
    private int totalApplications;
    private double averageApplicationsPerJob;

    public StatResponse(UUID id, String category, int totalJobPosts, int totalApplications) {
        this.id = id;
        this.category = category;
        this.totalJobPosts = totalJobPosts;
        this.totalApplications = totalApplications;
        this.averageApplicationsPerJob = totalJobPosts > 0
                ? (double) totalApplications / totalJobPosts
                : 0.0;
    }
}
