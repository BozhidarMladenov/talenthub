package com.softuni.talenthub.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 60, message = "Full name must be 2-60 characters.")
    private String fullName;

    @Size(max = 500, message = "Bio must be at most 500 characters.")
    private String bio;
}
