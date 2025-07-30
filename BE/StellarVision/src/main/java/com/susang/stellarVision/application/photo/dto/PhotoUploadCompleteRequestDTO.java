package com.susang.stellarVision.application.photo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoUploadCompleteRequestDTO {
    private Long memberId;
    private String originalFilename;
    private String s3Key;
}
