package com.susang.stellarVision.application.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VideoSearchRequest {
    private String keyword;
}
