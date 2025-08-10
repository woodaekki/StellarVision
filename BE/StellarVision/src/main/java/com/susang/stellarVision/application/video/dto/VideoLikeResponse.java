package com.susang.stellarVision.application.video.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoLikeResponse {
    private Long videoId;
    private boolean liked;
    private long likeCount;
}
