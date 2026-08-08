package com.softuni.statssvc.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatRecordRequest {

    @NotBlank(message = "Category is required.")
    private String category;

    private int jobPostDelta;
    private int applicationDelta;
}
