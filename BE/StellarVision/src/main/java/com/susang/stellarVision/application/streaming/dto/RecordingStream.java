package com.susang.stellarVision.application.streaming.dto;

import java.io.InputStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecordingStream {
    private Long contentLength;
    private InputStream inputStream;
}
