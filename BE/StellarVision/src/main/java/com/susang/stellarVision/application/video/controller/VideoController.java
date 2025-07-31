package com.susang.stellarVision.application.video.controller;


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
        return APIResponse.success("다시보기 영상 URL 발급 성공",replayUrl);
    }
    @DeleteMapping("/{videoId}")
    public APIResponse<String> deleteVideo(@PathVariable Long videoId) {
        videoService.deleteVideo(videoId);
        return APIResponse.success("다시보기 영상 삭제 성공", null);
    }

}