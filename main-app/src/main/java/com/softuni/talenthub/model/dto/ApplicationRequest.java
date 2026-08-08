package com.softuni.talenthub.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ApplicationRequest {

    @NotBlank(message = "Cover letter is required.")
    @Size(min = 20, max = 1000, message = "Cover letter must be 20-1000 characters.")
    private String coverLetter;

    @NotNull(message = "Proposed rate is required.")
    @DecimalMin(value = "1.00", message = "Proposed rate must be at least $1.")
    private BigDecimal proposedRate;
}
