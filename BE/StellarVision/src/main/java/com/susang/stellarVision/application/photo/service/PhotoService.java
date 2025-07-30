package com.susang.stellarVision.application.photo.service;

import com.susang.stellarVision.application.photo.dto.PhotoResponseDTO;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponseDTO;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PhotoService {

    PhotoUploadResponseDTO getPresignedUploadUrl(Long memberId, String originalFilename);
    String getPresignedDownloadUrl(String photoS3Key);

    // Todo : 메서드 구현
    void savePhotoMeta(Long memberId, String originalFilename, String s3Key);

    // 조회
    PhotoResponseDTO getPhoto(Long photoId);
    Page<PhotoResponseDTO> getPhotosByMemberId(Long memberId, Pageable pageable);
    // 삭제
    void deletePhoto(Long photoId);

}
