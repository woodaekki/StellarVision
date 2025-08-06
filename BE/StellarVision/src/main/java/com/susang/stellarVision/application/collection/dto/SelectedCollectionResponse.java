package com.susang.stellarVision.application.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SelectedCollectionResponse {
    private Integer id;
    private String name;
    private String koreanName;
    private String abbreviation;
    private boolean isSelect;
}
