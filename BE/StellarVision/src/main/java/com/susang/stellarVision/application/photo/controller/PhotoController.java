package com.susang.stellarVision.application.photo.controller;

import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoTagListResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;
import com.susang.stellarVision.application.photo.service.PhotoService;
import com.susang.stellarVision.application.video.dto.VideoTagListResponse;
import com.susang.stellarVision.common.dto.APIResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping("/presignedUrl")
    public ResponseEntity<APIResponse<PhotoUploadResponse>> getUploadUrl(
            @RequestBody PhotoUploadRequest request) {
        PhotoUploadResponse response = photoService.getGalleryPresignedUploadUrl(
                request.getMemberId(), request.getOriginalFilename());

        return ResponseEntity.ok(APIResponse.success(response));
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<APIResponse<PhotoResponse>> getPhoto(@PathVariable Long photoId) {
        PhotoResponse response = photoService.getPhoto(photoId);
        return ResponseEntity.ok(APIResponse.success(response));
    }

    @PostMapping("/complete")
    public ResponseEntity<APIResponse<PhotoUploadCompleteResponse>> completeUpload(
            @RequestBody PhotoUploadCompleteRequest request) {

        PhotoUploadCompleteResponse result = photoService.completeUpload(request);
        return ResponseEntity.ok(APIResponse.success("갤러리 사진 업로드 완료 및 메타데이터 저장 성공", result));
    }

    @DeleteMapping("{photoId}")
    public ResponseEntity<APIResponse<String>> deletePhoto(@PathVariable Long photoId) {
        photoService.deletePhoto(photoId);
        return ResponseEntity.ok(APIResponse.success("사진 삭제 성공", null));
    }

    @GetMapping("{photoId}/tags")
    public ResponseEntity<APIResponse<PhotoTagListResponse>> getTags(@PathVariable Long photoId) {
        PhotoTagListResponse photoTagListResponse = photoService.getTagsByPhotoId(photoId);
        return ResponseEntity.ok(APIResponse.success("태그 목록 조회 성공", photoTagListResponse));

    }


}
