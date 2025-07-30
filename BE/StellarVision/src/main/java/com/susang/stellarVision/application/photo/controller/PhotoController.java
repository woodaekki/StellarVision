package com.susang.stellarVision.application.photo.controller;

import com.susang.stellarVision.application.photo.dto.PhotoResponseDTO;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteRequestDTO;
import com.susang.stellarVision.application.photo.dto.PhotoUploadRequestDTO;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponseDTO;
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
    public APIResponse<PhotoUploadResponseDTO> getUploadUrl(@RequestBody PhotoUploadRequestDTO request) {
        PhotoUploadResponseDTO response = photoService.getPresignedUploadUrl(
                request.getMemberId(),
                request.getOriginalFilename()
        );

        return APIResponse.success(response);
    }

    @GetMapping("/{photoId}")
    public APIResponse<PhotoResponseDTO> getPhoto(@PathVariable Long photoId) {
        PhotoResponseDTO response = photoService.getPhoto(photoId);
        return APIResponse.success(response);
    }

    @PostMapping("/complete")
    public APIResponse<String> completeUpload(@RequestBody PhotoUploadCompleteRequestDTO request) {

            photoService.savePhotoMeta(
                    request.getMemberId(),
                    request.getOriginalFilename(),
                    request.getS3Key()
            );
            return APIResponse.success("업로드 완료 및 메타데이터 저장 성공",null);


}


}
