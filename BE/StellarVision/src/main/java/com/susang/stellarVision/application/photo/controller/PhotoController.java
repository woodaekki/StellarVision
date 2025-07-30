package com.susang.stellarVision.application.photo.controller;

import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;
import com.susang.stellarVision.application.photo.service.PhotoService;
import com.susang.stellarVision.common.dto.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping("/presignedUrl")
    public APIResponse<PhotoUploadResponse> getUploadUrl(@RequestBody PhotoUploadRequest request) {
        PhotoUploadResponse response = photoService.getGalleryPresignedUploadUrl(
                request.getMemberId(), request.getOriginalFilename());

        return APIResponse.success(response);
    }

    @GetMapping("/{photoId}")
    public APIResponse<PhotoResponse> getPhoto(@PathVariable Long photoId) {
        PhotoResponse response = photoService.getPhoto(photoId);
        return APIResponse.success(response);
    }

    @PostMapping("/complete")
    public APIResponse<String> completeUpload(@RequestBody PhotoUploadCompleteRequest request) {

        photoService.savePhotoMeta(request.getMemberId(), request.getOriginalFilename(),
                request.getS3Key());
        return APIResponse.success("갤러리 사진 업로드 완료 및 메타데이터 저장 성공", null);
    }

    @DeleteMapping("{photoId}")
    public APIResponse<String> deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return APIResponse.success("사진 삭제 성공", null);
    }


}
