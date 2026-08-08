package com.softuni.talenthub.model.dto;

import com.softuni.talenthub.model.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please provide a valid email.")
    private String email;

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, max = 60, message = "Full name must be 2-60 characters.")
    private String fullName;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, max = 40, message = "Password must be 6-40 characters.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    @NotNull(message = "Please select a role.")
    private UserRole role;
}
