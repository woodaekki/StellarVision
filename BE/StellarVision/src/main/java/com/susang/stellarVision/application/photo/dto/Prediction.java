package com.susang.stellarVision.application.photo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Prediction {
    @JsonProperty("class")
    private String className;

    private double confidence;
    private List<Double> bbox;
}
