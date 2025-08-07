package com.susang.stellarVision.application.photo.dto;

import com.susang.stellarVision.application.collection.dto.CollectionListResponse;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PhotoUploadCompleteResponse {
    private String title;
    private LocalDateTime creationDate;
    private PhotoTagListResponse tagList;
    private CollectionListResponse collectionList;
}