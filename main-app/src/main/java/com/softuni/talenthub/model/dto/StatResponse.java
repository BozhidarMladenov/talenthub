package com.softuni.talenthub.model.dto;

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
}
