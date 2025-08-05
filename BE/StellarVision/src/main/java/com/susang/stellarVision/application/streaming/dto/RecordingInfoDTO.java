package com.susang.stellarVision.application.streaming.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecordingInfoDTO {
    private String recordingId;
    private String recordingUrl;
    private String recordingName;
    private LocalDateTime recordedAt;
}
