package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.streaming.dto.RecordingStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordingDownloadService {

    @Value("${openvidu.secret}")
    private String secret;

    private final WebClient webClient;

    @Async("recordingExecutor")
    public CompletableFuture<RecordingStream> getRecordingStream(String recordingUrl) {
        // 1) 요청 및 응답 수신
        ResponseEntity<byte[]> response = webClient.get()
                .uri(recordingUrl)
                .headers(h -> h.setBasicAuth("OPENVIDUAPP", secret))
                .retrieve()
                .toEntity(byte[].class)
                .block();

        if (response == null) {
            throw new RuntimeException("다운로드 응답이 없습니다");
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("HTTP 에러: " + response.getStatusCode());
        }

        long contentLengthHeader = response.getHeaders().getContentLength();

        byte[] data = response.getBody();
        if (data == null || data.length == 0) {
            throw new RuntimeException("다운로드 받은 데이터가 없습니다");
        }

        // 4) InputStream 생성 및 반환
        InputStream is = new ByteArrayInputStream(data);
        return CompletableFuture.completedFuture(
                new RecordingStream(contentLengthHeader, is)
        );
    }
}
