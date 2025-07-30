package com.susang.stellarVision.application.profile.controller;

import com.susang.stellarVision.application.photo.dto.PhotoListResponseDTO;
import com.susang.stellarVision.application.photo.dto.PhotoResponseDTO;
import com.susang.stellarVision.application.photo.service.PhotoService;
import com.susang.stellarVision.common.dto.APIResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final PhotoService photoService;


    @GetMapping("/{memberId}/photos")
    public APIResponse<PhotoListResponseDTO> getPhotosByMemberId(
            @PathVariable Long memberId,
            @PageableDefault(size = 3, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<PhotoResponseDTO> page = photoService.getPhotosByMemberId(memberId, pageable);
        PhotoListResponseDTO response = new PhotoListResponseDTO(page.getContent(), page.getTotalElements());
        return APIResponse.success(response);
    }
}
