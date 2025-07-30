package com.susang.stellarVision.application.streaming.controller;

import com.susang.stellarVision.application.streaming.dto.CreateStreamingSessionRequest;
import com.susang.stellarVision.application.streaming.service.StreamingService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.common.security.dto.CustomUserDetails;
import com.susang.stellarVision.entity.Member;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/streamings")
@RequiredArgsConstructor
@Slf4j
public class StreamingController {

    private final StreamingService streamingService;

    @PostMapping
    public ResponseEntity<APIResponse<String>> createStreamingSession(
            @Valid @RequestBody(required = false) CreateStreamingSessionRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        try {
            Member member = customUserDetails.getMember();
            String session = streamingService.createSession(request, member);
            return ResponseEntity.ok(APIResponse.success(session));
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail("STREAM", "스트리밍 방 생성에 실패했습니다.", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/connection")
    public ResponseEntity<APIResponse<String>> createStreamingToken(
            @PathVariable String sessionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member member = customUserDetails.getMember();
        try {
            String token = streamingService.createToken(sessionId, member);
            return ResponseEntity.ok(APIResponse.success(token));
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail("STREAM", "스트리밍 토큰 생성에 실패했습니다.", e.getMessage()));
        }
    }
}
