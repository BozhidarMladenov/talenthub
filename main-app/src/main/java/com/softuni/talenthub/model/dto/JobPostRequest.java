package com.softuni.talenthub.model.dto;

import com.softuni.talenthub.model.enums.JobCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class JobPostRequest {

    @NotBlank(message = "Title is required.")
    @Size(min = 5, max = 100, message = "Title must be 5-100 characters.")
    private String title;

    @NotBlank(message = "Description is required.")
    @Size(min = 20, max = 2000, message = "Description must be 20-2000 characters.")
    private String description;

    @NotNull(message = "Category is required.")
    private JobCategory category;

    @NotNull(message = "Budget is required.")
    @DecimalMin(value = "1.00", message = "Budget must be at least $1.")
    @DecimalMax(value = "1000000.00", message = "Budget is unrealistically high.")
    private BigDecimal budget;
}
