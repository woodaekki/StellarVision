package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.streaming.dto.CreateStreamingSessionRequest;
import com.susang.stellarVision.application.streaming.dto.StreamingRoomDTO;
import com.susang.stellarVision.entity.Member;
import io.openvidu.java.client.OpenViduException;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import java.util.List;
import java.util.Map;

public interface StreamingService {

    String createSession(CreateStreamingSessionRequest request, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;

    String createToken(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;

    void deleteSession(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException;

    List<StreamingRoomDTO> getStreamingRoomList();
}
