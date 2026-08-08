package com.softuni.talenthub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatRecordRequest {

    private String category;
    private int jobPostDelta;
    private int applicationDelta;
}
