package com.susang.stellarVision.application.streaming.controller;

import com.susang.stellarVision.application.streaming.dto.AnalyzeTagsRequest;
import com.susang.stellarVision.application.streaming.dto.ConnectionTokenDTO;
import com.susang.stellarVision.application.streaming.dto.CreateStreamingSessionRequest;
import com.susang.stellarVision.application.streaming.dto.RecordingInfoDTO;
import com.susang.stellarVision.application.streaming.dto.StreamingRoomDTO;
import com.susang.stellarVision.common.exception.AccessDeniedException;
import com.susang.stellarVision.application.streaming.exception.RecordingInProgressException;
import com.susang.stellarVision.application.streaming.service.StreamingService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.entity.Member;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<APIResponse<ConnectionTokenDTO>> createStreamingToken(
            @PathVariable String sessionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Member member = customUserDetails.getMember();
        try {
            ConnectionTokenDTO data = streamingService.createToken(sessionId, member);
            return ResponseEntity.ok(APIResponse.success(data));
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail("STREAM", "스트리밍 토큰 생성에 실패했습니다.", e.getMessage()));
        }
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<APIResponse<String>> deleteStreamingSession(
            @PathVariable String sessionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        try {
            Member member = customUserDetails.getMember();
            streamingService.deleteSession(sessionId, member);
            return ResponseEntity.ok(APIResponse.success(null));
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail("STREAM", "스트리밍 종료에 실패했습니다.", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<StreamingRoomDTO>>> getStreamingList() {
        List<StreamingRoomDTO> streamingRoomList = streamingService.getStreamingRoomList();
        return ResponseEntity.ok(APIResponse.success(streamingRoomList));
    }

    @PostMapping("/{sessionId}/recordings/start")
    public ResponseEntity<APIResponse<String>> startStreamingRecording(
            @PathVariable String sessionId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        try {
            Member member = customUserDetails.getMember();
            String recordingId = streamingService.startRecording(sessionId, member);
            return ResponseEntity.ok(APIResponse.success(recordingId));
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail("RECORDING", "녹화 시작에 실패했습니다.", e.getMessage()));
        }
    }

    @PostMapping("/recordings/{recordingId}/stop")
    public ResponseEntity<APIResponse<RecordingInfoDTO>> endStreamingRecording(
            @PathVariable String recordingId,
            @RequestBody AnalyzeTagsRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        try {
            Member member = customUserDetails.getMember();
            List<String> tags = request.getTags();
            RecordingInfoDTO recordingInfoDTO = streamingService.stopRecording(recordingId, member, tags);
            return ResponseEntity.ok(APIResponse.success("녹화저장에 성공했습니다.", recordingInfoDTO));
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(APIResponse.fail("RECORDING", "녹화 종료에 실패했습니다.", e.getMessage()));
        }
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(APIResponse.fail("STREAM", e.getMessage()));
    }

    @ExceptionHandler(RecordingInProgressException.class)
    public ResponseEntity<APIResponse<Void>> handleRecordingInProgress(RecordingInProgressException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(APIResponse.fail("STREAM", e.getMessage()));
    }
}
