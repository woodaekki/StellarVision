package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.streaming.dto.CreateStreamingSessionRequest;
import com.susang.stellarVision.application.streaming.exception.SessionNotFoundException;
import com.susang.stellarVision.application.streaming.repository.StreamingRepository;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.StreamingRoom;
import io.openvidu.java.client.*;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {

    private final OpenVidu openVidu;
    private final StreamingRepository streamingRepository;

    @Override
    @Transactional
    public String createSession(CreateStreamingSessionRequest request, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException {

        if (request.getForcedVideoCodec() == VideoCodec.MEDIA_SERVER_PREFERRED) {
            request.setForcedVideoCodec(VideoCodec.VP8);
        }

        SessionProperties.Builder builder = new SessionProperties.Builder()
                .forcedVideoCodec(VideoCodec.valueOf(request.getForcedVideoCodec()
                        .name()))
                .mediaMode(MediaMode.valueOf(request.getMediaMode()
                        .name()))
                .recordingMode(RecordingMode.valueOf(request.getRecordingMode()
                        .name()));

        SessionProperties properties = builder.build();
        Session session = openVidu.createSession(properties);

        StreamingRoom streamingRoom = StreamingRoom.builder()
                .sessionId(session.getSessionId())
                .title(request.getTitle())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .build();

        streamingRepository.save(streamingRoom);

        return session.getSessionId();
    }

    @Override
    @Transactional(readOnly = true)
    public String createToken(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException {
        StreamingRoom streamingRoom = streamingRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("해당 세션을 찾을 수 없습니다."));

        Session session = openVidu.getActiveSession(streamingRoom.getSessionId());

        OpenViduRole role;
        if (Objects.equals(member.getId(), streamingRoom.getMember()
                .getId())) {
            // 스트리머 이므로 권한을 PUBLIC
            role = OpenViduRole.PUBLISHER;
        } else {
            role = OpenViduRole.SUBSCRIBER;
        }

        ConnectionProperties connectionProperties = new ConnectionProperties.Builder()
                .type(ConnectionType.WEBRTC)
                .role(role)
                .build();

        Connection connection = session.createConnection(connectionProperties);

        return connection.getToken();
    }
}
