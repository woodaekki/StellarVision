package com.susang.stellarVision.application.video.controller;


import com.susang.stellarVision.application.video.dto.*;
import com.susang.stellarVision.application.video.service.VideoService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping("/{videoId}")
    public ResponseEntity<APIResponse<String>> getVideoPresignedUrl(@PathVariable Long videoId) {
        String replayUrl = videoService.getVideoPresignedUrl(videoId);
        return ResponseEntity.ok(APIResponse.success("다시보기 영상 URL 발급 성공", replayUrl));
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<APIResponse<String>> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
        return ResponseEntity.ok(APIResponse.success("다시보기 영상 삭제 성공", null));
    }

    @PostMapping("/{videoId}/tags")
    public ResponseEntity<APIResponse<String>> addVideoTag(@PathVariable Long videoId,@RequestBody VideoTagRequest videoTagRequest) {
        videoService.addVideoTag(videoId,videoTagRequest);
        return ResponseEntity.ok(APIResponse.success("태그 추가 성공", null));
    }

    @GetMapping("/{videoId}/tags")
    public ResponseEntity<APIResponse<VideoTagListResponse>> getVideoTag(@PathVariable Long videoId) {
        VideoTagListResponse videoTagListResponse = videoService.getTagsByVideoId(videoId);
        return ResponseEntity.ok(APIResponse.success("태그 목록 조회 성공", videoTagListResponse));
    }
    @DeleteMapping("/{videoId}/tags/{tagId}")
    public ResponseEntity<APIResponse<String>> deleteVideoTag(@PathVariable Long videoId, @PathVariable Long tagId) {
        videoService.deleteVideoTag(videoId,tagId);
        return ResponseEntity.ok(APIResponse.success("태그 삭제 성공"));
    }

    @GetMapping()
    public ResponseEntity<APIResponse<VideoListResponse>> getVideoList( @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
    Pageable pageable, @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = (user != null) ? user.getMember().getId() : null;
        Page<VideoResponse> page = videoService.getVideos(pageable, memberId);
        VideoListResponse response = new VideoListResponse(page.getContent(),
                page.getTotalElements());

        return ResponseEntity.ok(APIResponse.success(response));


    }

    @GetMapping("/search")
    public ResponseEntity<APIResponse<VideoListResponse>> searchVideos(@ModelAttribute VideoSearchRequest condition,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable, @AuthenticationPrincipal CustomUserDetails user) {
        Long memberId = (user != null) ? user.getMember().getId() : null;
        Page<VideoResponse> page = videoService.searchVideos(condition, pageable, memberId);

        VideoListResponse response = new VideoListResponse(page.getContent(),
                page.getTotalElements());

        return ResponseEntity.ok(APIResponse.success(response));
    }

    @PostMapping("/{videoId}/likes")
    public ResponseEntity<APIResponse<VideoLikeResponse>>  like(@PathVariable Long videoId,
                                  @AuthenticationPrincipal CustomUserDetails user) {
        VideoLikeResponse videoLikeResponse = videoService.like(videoId, user.getMember().getId());
        return ResponseEntity.ok(APIResponse.success("영상 좋아요 성공", videoLikeResponse));
    }

    @DeleteMapping("/{videoId}/likes")
    public ResponseEntity<APIResponse<VideoLikeResponse>> unlike(@PathVariable Long videoId,
                               @AuthenticationPrincipal CustomUserDetails user) {
        VideoLikeResponse videoLikeResponse =  videoService.unlike(videoId, user.getMember().getId());
        return ResponseEntity.ok(APIResponse.success("영상 좋아요 취소 성공", videoLikeResponse));
    }

}