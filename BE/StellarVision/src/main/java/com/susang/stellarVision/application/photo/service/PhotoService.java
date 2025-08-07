package com.susang.stellarVision.application.photo.service;

import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoTagListResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;

import com.susang.stellarVision.application.photo.error.PhotoNotFoundException;
import com.susang.stellarVision.common.s3.S3Directory;
import com.susang.stellarVision.entity.Collection;
import com.susang.stellarVision.entity.Photo;
import com.susang.stellarVision.entity.PhotoTag;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PhotoService {

    PhotoUploadResponse generatePresignedUploadUrl(S3Directory directory, Long memberId,
            String originalFilename);

    PhotoUploadResponse getGalleryPresignedUploadUrl(Long memberId, String originalFilename);

    PhotoUploadResponse getProfilePresignedUploadUrl(Long memberId, String originalFilename);

    Photo savePhotoMeta(Long memberId, String originalFilename, String s3Key);

    PhotoResponse getPhoto(Long photoId) throws PhotoNotFoundException;

    Page<PhotoResponse> getPhotosByMemberId(Long memberId, Pageable pageable);

    void deletePhoto(Long photoId);

    PhotoTagListResponse getTagsByPhotoId(Long photoId);

    PhotoUploadCompleteResponse completeUpload(PhotoUploadCompleteRequest request);

    List<Collection> saveTagsAndCollections(Long photoId, List<String> tags);



}
