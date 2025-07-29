package com.susang.stellarVision.application.photo.controller;

import com.susang.stellarVision.application.photo.dto.PhotoUploadRequest;
import com.susang.stellarVision.application.photo.service.PhotoService;
import com.susang.stellarVision.common.dto.APIResponse;
import javax.print.DocFlavor.URL;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping("/presignedUrl")
    public ResponseEntity<APIResponse<String>> getUploadUrl(@RequestBody PhotoUploadRequest request) {
        String uploadUrl = photoService.getPresignedUploadUrl(request.getMemberId(), request.getOriginalFilename());

        return ResponseEntity.ok(APIResponse.success(uploadUrl));
    }
}
