package com.susang.stellarVision.application.photo.dto;

import lombok.Getter;

@Getter
public class PhotoUploadRequest {

    private Long memberId;
    private String originalFilename;
}
