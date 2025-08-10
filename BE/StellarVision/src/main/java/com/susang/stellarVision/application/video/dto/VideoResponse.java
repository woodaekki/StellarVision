package com.susang.stellarVision.application.video.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoResponse {
        private Long id;
        private String originalFilename;
        private LocalDateTime createdAt;
        private String thumbnailDownloadUrl;
        private Long memberId;
        private String nickname;
        private long likeCount;
        private boolean liked;
}
