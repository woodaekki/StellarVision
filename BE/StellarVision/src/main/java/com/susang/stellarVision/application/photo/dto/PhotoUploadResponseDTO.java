package com.susang.stellarVision.application.photo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoUploadResponseDTO {
    private String uploadUrl;
    private String s3Key;
}