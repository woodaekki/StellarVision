package com.susang.stellarVision.application.profile.controller;

import com.susang.stellarVision.application.collection.dto.CollectionListResponse;
import com.susang.stellarVision.application.collection.dto.CollectionResponse;
import com.susang.stellarVision.application.collection.dto.SelectCollectionRequest;
import com.susang.stellarVision.application.collection.dto.SelectedCollectionListResponse;
import com.susang.stellarVision.application.collection.dto.SelectedCollectionResponse;
import com.susang.stellarVision.application.collection.service.CollectionService;
import com.susang.stellarVision.application.photo.dto.PhotoListResponse;
import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.photo.dto.PhotoUploadCompleteRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadRequest;
import com.susang.stellarVision.application.photo.dto.PhotoUploadResponse;
import com.susang.stellarVision.application.photo.service.PhotoService;
import com.susang.stellarVision.application.profile.dto.ProfileResponse;
import com.susang.stellarVision.application.profile.dto.ProfileVisibilityUpdateRequest;
import com.susang.stellarVision.application.profile.service.ProfileService;
import com.susang.stellarVision.application.video.dto.VideoListResponse;
import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.application.video.dto.VideoUpdateRequest;
import com.susang.stellarVision.application.video.service.VideoService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.entity.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final CollectionService collectionService;

    @PostMapping("/presignedUrl")
    public ResponseEntity<APIResponse<PhotoUploadResponse>> getProfilePresignedUploadUrl(
            @RequestBody PhotoUploadRequest request) {
        PhotoUploadResponse response = photoService.getProfilePresignedUploadUrl(
                request.getMemberId(), request.getOriginalFilename());
        return ResponseEntity.ok(APIResponse.success(response));
    }

    @PostMapping("/complete")
    public ResponseEntity<APIResponse<String>> completeUpload(
            @RequestBody PhotoUploadCompleteRequest request) {

        profileService.saveProfileImageMeta(request.getMemberId(), request.getOriginalFilename(),
                request.getS3Key());

        return ResponseEntity.ok(APIResponse.success("프로필 이미지 업로드 완료 및 메타데이터 저장 성공", null));
    }


    @GetMapping("/{memberId}/photos")
    public ResponseEntity<APIResponse<PhotoListResponse>> getPhotosByMemberId(
            @PathVariable Long memberId,
            @PageableDefault(size = 3, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PhotoResponse> page = photoService.getPhotosByMemberId(memberId, pageable);
        PhotoListResponse response = new PhotoListResponse(page.getContent(),
                page.getTotalElements());
        return ResponseEntity.ok(APIResponse.success(response));
    }

    @GetMapping("{memberId}/videos")
    public ResponseEntity<APIResponse<VideoListResponse>> getVideosByMemberId(
            @PathVariable Long memberId,
            @PageableDefault(size = 3, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<VideoResponse> page = videoService.getVideosByMemberId(memberId, pageable);
        VideoListResponse response = new VideoListResponse(page.getContent(),
                page.getTotalElements());

        return ResponseEntity.ok(APIResponse.success(response));
    }


    @GetMapping("/{memberId}/image")
    public ResponseEntity<APIResponse<String>> getProfileImage(@PathVariable Long memberId) {
        String downloadUrl = profileService.getProfileImage(memberId);
        return ResponseEntity.ok(APIResponse.success(downloadUrl));
    }

    @DeleteMapping("/{memberId}/image")
    public ResponseEntity<APIResponse<String>> deleteProfileImage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        profileService.deleteProfileImage(userDetails);
        return ResponseEntity.ok(APIResponse.success("프로필 이미지 삭제 성공",null));
    }

    @PatchMapping("/videos/{videoId}")
    public ResponseEntity<APIResponse<String>> updateVideoContent(@PathVariable Long videoId,
            @RequestBody VideoUpdateRequest request) {
        videoService.updateVideoContent(videoId, request);
        return ResponseEntity.ok(APIResponse.success("다시보기 제목,태그 수정 성공", null));
    }

    @GetMapping("/me")
    public ResponseEntity<APIResponse<ProfileResponse>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProfileResponse response = profileService.getMyProfileInfo(userDetails);
        return ResponseEntity.ok(APIResponse.success("내 프로필 정보 조회 성공", response));
    }


    @GetMapping("/{memberId}")
    public ResponseEntity<APIResponse<ProfileResponse>> getProfileInfo(
            @PathVariable Long memberId) {
        ProfileResponse response = profileService.getProfileInfo(memberId);
        return ResponseEntity.ok(APIResponse.success("프로필 정보 조회 성공", response));
    }

    @PatchMapping("/me/visibility")
    public ResponseEntity<APIResponse<String>> updateVisibility(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileVisibilityUpdateRequest profileVisibilityUpdateRequest) {
        profileService.updateVisibility(userDetails, profileVisibilityUpdateRequest);
        return ResponseEntity.ok(APIResponse.success("프로필 공개 여부 변경 성공", null));
    }

    @GetMapping("/{memberId}/collections")
    public ResponseEntity<APIResponse<CollectionListResponse>> getMyCollections(
            @PathVariable Long memberId) {
        CollectionListResponse response = collectionService.getCollectionsByMemberId(memberId);
        return ResponseEntity.ok(APIResponse.success(response));

    }

    @PatchMapping("/me/badge")
    public ResponseEntity<APIResponse<String>> updateMyBadge(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SelectCollectionRequest selectCollectionRequest) {
        collectionService.updateMyBadge(userDetails, selectCollectionRequest);
        return ResponseEntity.ok(APIResponse.success("뱃지 설정 성공", null));
    }

    @GetMapping("/{memberId}/badge")
    public ResponseEntity<APIResponse<SelectedCollectionListResponse>> getBadges(
            @PathVariable Long memberId) {
        SelectedCollectionListResponse selectedCollectionListResponse = collectionService.getBadges(
                memberId);
        return ResponseEntity.ok(APIResponse.success("뱃지 조회 성공", selectedCollectionListResponse));
    }


}




