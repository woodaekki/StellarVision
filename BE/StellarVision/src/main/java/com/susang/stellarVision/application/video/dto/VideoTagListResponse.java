package com.susang.stellarVision.application.video.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoTagListResponse {

    private List<VideoTagResponse> tags;
}
