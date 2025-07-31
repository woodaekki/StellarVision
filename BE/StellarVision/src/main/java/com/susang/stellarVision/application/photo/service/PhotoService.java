package com.susang.stellarVision.application.photo.service;

import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;

import com.susang.stellarVision.application.photo.error.PhotoNotFoundException;
import com.susang.stellarVision.common.s3.S3Directory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PhotoService {

    public PhotoUploadResponse generatePresignedUploadUrl(S3Directory directory, Long memberId,
            String originalFilename);

    public PhotoUploadResponse getGalleryPresignedUploadUrl(Long memberId, String originalFilename);

    public PhotoUploadResponse getProfilePresignedUploadUrl(Long memberId, String originalFilename);

    void savePhotoMeta(Long memberId, String originalFilename, String s3Key);

    PhotoResponse getPhoto(Long photoId) throws PhotoNotFoundException;

    Page<PhotoResponse> getPhotosByMemberId(Long memberId, Pageable pageable);

    void deletePhoto(Long photoId);

}
