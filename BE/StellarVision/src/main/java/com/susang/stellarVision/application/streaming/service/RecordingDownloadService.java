package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.streaming.dto.RecordingStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RecordingDownloadService {

    @Value("${openvidu.secret}")
    private String secret;

    private final WebClient webClient;

    @Async("recordingExecutor")
    public CompletableFuture<RecordingStream> getRecordingStream(String recordingUrl) {
        ClientResponse resp = webClient.get()
                .uri(recordingUrl)
                .headers(h -> h.setBasicAuth("OPENVIDUAPP", secret))
                .exchangeToMono(Mono::just)
                .block();

        if (resp == null || !resp.statusCode()
                .is2xxSuccessful()) {
            throw new RuntimeException(
                    "다운로드 실패: " + (resp != null ? resp.statusCode() : "No response"));
        }

        Long contentLength = resp.headers()
                .contentLength()
                .orElse(-1L);

        InputStream inputStream = DataBufferUtils.join(resp.bodyToFlux(DataBuffer.class))
                .map(DataBuffer::asInputStream)
                .block();

        if (inputStream == null) {
            throw new RuntimeException("InputStream 생성실패");
        }

        return CompletableFuture.completedFuture(new RecordingStream(contentLength, inputStream));
    }
}
