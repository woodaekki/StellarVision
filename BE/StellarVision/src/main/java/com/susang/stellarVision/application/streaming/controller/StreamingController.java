package com.susang.stellarVision.application.streaming.controller;

import com.susang.stellarVision.application.streaming.service.StreamingService;
import com.susang.stellarVision.common.dto.APIResponse;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/streamings")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    @PostMapping
    public ResponseEntity<APIResponse<String>> createStreamingSession(@RequestBody(required = false) Map<String, Object> params) {
        try {
            String session = streamingService.createSession(params);
            return ResponseEntity.ok(APIResponse.success(session));
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail("STREAM", "스트리밍 방 생성에 실패했습니다.", e.getMessage()));
        }
    }

}
