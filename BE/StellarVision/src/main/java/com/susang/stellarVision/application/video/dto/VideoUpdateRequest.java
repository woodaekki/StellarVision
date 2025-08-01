package com.susang.stellarVision.application.video.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoUpdateRequest {
    private String title;
    private List<VideoTagRequest> tags;
}
