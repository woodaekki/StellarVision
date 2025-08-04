package com.susang.stellarVision.application.collection.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionListResponse {
    private List<CollectionResponse> collections;
}