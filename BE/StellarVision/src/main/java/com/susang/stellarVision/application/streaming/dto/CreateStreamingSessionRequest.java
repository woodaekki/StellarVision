package com.susang.stellarVision.application.streaming.dto;

import io.openvidu.java.client.MediaMode;
import io.openvidu.java.client.RecordingMode;
import io.openvidu.java.client.VideoCodec;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStreamingSessionRequest {

    @NotBlank
    private String title;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;
}
