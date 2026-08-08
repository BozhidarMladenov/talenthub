package com.softuni.talenthub.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatResponseCollection {

    @JsonProperty("_embedded")
    private Embedded embedded;

    public List<StatResponse> getContent() {
        if (embedded == null || embedded.getStatResponseList() == null) {
            return new ArrayList<>();
        }
        return embedded.getStatResponseList();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Embedded {

        @JsonProperty("statResponseList")
        private List<StatResponse> statResponseList;
    }
}
