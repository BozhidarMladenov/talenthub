package com.softuni.talenthub.model.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating cannot exceed 5.")
    private Integer rating;

    @Size(max = 500, message = "Comment must be at most 500 characters.")
    private String comment;
}
