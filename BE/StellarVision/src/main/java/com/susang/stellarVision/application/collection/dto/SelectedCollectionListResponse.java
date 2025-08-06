package com.susang.stellarVision.application.collection.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SelectedCollectionListResponse {

    List<SelectedCollectionResponse> selectedCollections;
}
