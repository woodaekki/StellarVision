package com.susang.stellarVision.application.photo.service;

public interface PhotoService {
    String getPresignedUploadUrl(Long memberId, String originalFilename);
    String getPresignedDownloadUrl(String PhotoS3Key);
}
