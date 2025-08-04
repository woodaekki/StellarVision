package com.susang.stellarVision.application.profile.controller;

import com.susang.stellarVision.application.photo.dto.PhotoListResponse;
import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;
import com.susang.stellarVision.application.photo.service.PhotoService;
import com.susang.stellarVision.application.profile.dto.ProfileResponse;
import com.susang.stellarVision.application.profile.service.ProfileService;
import com.susang.stellarVision.application.video.dto.VideoListResponse;
import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.application.video.dto.VideoUpdateRequest;
import com.susang.stellarVision.application.video.service.VideoService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final PhotoService photoService;
    private final ProfileService profileService;
    private final VideoService videoService;


    @PostMapping("/presignedUrl")
    public APIResponse<PhotoUploadResponse> getProfilePresignedUploadUrl(
            @RequestBody PhotoUploadRequest request) {
        PhotoUploadResponse response = photoService.getProfilePresignedUploadUrl(
                request.getMemberId(), request.getOriginalFilename());
        return APIResponse.success(response);
    }

    @PostMapping("/complete")
    public APIResponse<String> completeUpload(@RequestBody PhotoUploadCompleteRequest request) {

        profileService.saveProfileImageMeta(
                request.getMemberId(),
                request.getOriginalFilename(),
                request.getS3Key());

        return APIResponse.success("프로필 이미지 업로드 완료 및 메타데이터 저장 성공", null);
    }


    @GetMapping("/{memberId}/photos")
    public APIResponse<PhotoListResponse> getPhotosByMemberId(
            @PathVariable Long memberId,
            @PageableDefault(size = 3, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<PhotoResponse> page = photoService.getPhotosByMemberId(memberId, pageable);
        PhotoListResponse response = new PhotoListResponse(page.getContent(),
                page.getTotalElements());
        return APIResponse.success(response);
    }

    @GetMapping("{memberId}/videos")
    public APIResponse<VideoListResponse> getVideosByMemberId(@PathVariable Long memberId,
            @PageableDefault(size = 3, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable){

        Page<VideoResponse> page = videoService.getVideosByMemberId(memberId, pageable);
        VideoListResponse response = new VideoListResponse(page.getContent(),
                page.getTotalElements());

        return APIResponse.success(response);
    }


    @GetMapping("/{memberId}/image")
    public APIResponse<String> getProfileImage(@PathVariable Long memberId) {
        String downloadUrl = profileService.getProfileImage(memberId);
        return APIResponse.success(downloadUrl);
    }

    @PatchMapping("/videos/{videoId}")
    public APIResponse<String> updateVideoContent (@PathVariable Long videoId,@RequestBody VideoUpdateRequest request) {
        videoService.updateVideoContent(videoId,request);
        return APIResponse.success("다시보기 제목,태그 수정 성공", null);
    }

    @GetMapping("/me")
    public APIResponse<ProfileResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails)  {
        ProfileResponse response = profileService.getMyProfileInfo(userDetails);
        return APIResponse.success("내 프로필 정보 조회 성공" ,response );
    }


    @GetMapping("/{memberId}")
    public APIResponse<ProfileResponse> getProfileInfo(@PathVariable Long memberId) {
        ProfileResponse response = profileService.getProfileInfo(memberId);
        return APIResponse.success("프로필 정보 조회 성공", response);
    }


    }




