package com.susang.stellarVision.application.video.controller;


import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.application.video.dto.VideoTagListResponse;
import com.susang.stellarVision.application.video.dto.VideoTagRequest;
import com.susang.stellarVision.application.video.service.VideoService;
import com.susang.stellarVision.common.dto.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @GetMapping("/{videoId}")
    public APIResponse<String> getVideoPresignedUrl(@PathVariable Long videoId) {
        String replayUrl = videoService.getVideoPresignedUrl(videoId);
        return APIResponse.success("다시보기 영상 URL 발급 성공", replayUrl);
    }

    @DeleteMapping("/{videoId}")
    public APIResponse<String> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
        return APIResponse.success("다시보기 영상 삭제 성공", null);
    }

    @PostMapping("/{videoId}/tags")
    public APIResponse<String> addVideoTag(@PathVariable Long videoId,@RequestBody VideoTagRequest videoTagRequest) {
        videoService.addVideoTag(videoId,videoTagRequest);
        return APIResponse.success("태그 추가 성공", null);
    }

    @GetMapping("/{videoId}/tags")
    public APIResponse<VideoTagListResponse> getVideoTag(@PathVariable Long videoId) {
        VideoTagListResponse videoTagListResponse = videoService.getTagsByVideoId(videoId);
        return APIResponse.success("태그 목록 조회 성공", videoTagListResponse);
    }
    @DeleteMapping("/{videoId}/tags/{tagId}")
    public APIResponse<String> deleteVideoTag(@PathVariable Long videoId, @PathVariable Long tagId) {
        videoService.deleteVideoTag(videoId,tagId);
        return APIResponse.success("태그 삭제 성공");
    }

}