package com.susang.stellarVision.application.video.dto;

import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoListResponse {
    private List<VideoResponse> videos;
    private long totalCount;
}
