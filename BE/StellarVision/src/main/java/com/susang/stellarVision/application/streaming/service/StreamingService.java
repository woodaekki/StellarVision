package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.streaming.dto.ConnectionTokenDTO;
import com.susang.stellarVision.application.streaming.dto.CreateStreamingSessionRequest;
import com.susang.stellarVision.application.streaming.dto.RecordingInfoDTO;
import com.susang.stellarVision.application.streaming.dto.StreamingRoomDTO;
import com.susang.stellarVision.entity.Member;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import java.util.List;

public interface StreamingService {

    String createSession(CreateStreamingSessionRequest request, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;

    ConnectionTokenDTO createToken(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;

    void deleteSession(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;

    List<StreamingRoomDTO> getStreamingRoomList();

    String startRecording(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;

    RecordingInfoDTO stopRecording(String recordingId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;
}
