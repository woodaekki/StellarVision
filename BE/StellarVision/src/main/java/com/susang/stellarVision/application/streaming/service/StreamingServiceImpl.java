package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.streaming.dto.CreateStreamingSessionRequest;
import com.susang.stellarVision.application.streaming.dto.StreamingRoomDTO;
import com.susang.stellarVision.application.streaming.exception.AccessDeniedException;
import com.susang.stellarVision.application.streaming.exception.SessionNotFoundException;
import com.susang.stellarVision.application.streaming.repository.StreamingRepository;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.StreamingRoom;
import io.openvidu.java.client.*;

import java.util.List;
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
                .member(member)
                .build();

        streamingRepository.save(streamingRoom);

        return session.getSessionId();
    }

    @Override
    @Transactional(readOnly = true)
    public String createToken(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException {
        StreamingRoom streamingRoom = streamingRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        Session session = openVidu.getActiveSession(streamingRoom.getSessionId());

        if (session == null) {
            throw new SessionNotFoundException(sessionId);
        }

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

    @Override
    @Transactional
    public void deleteSession(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException {

        StreamingRoom streamingRoom = streamingRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        // 스트리밍을 시작한 사람과 다르면 끌 수 없음
        if (!member.getId()
                .equals(streamingRoom.getMember()
                        .getId())) {
            throw new AccessDeniedException("생성한 멤버만 종료할 수 있습니다.");
        }

        Session activeSession = openVidu.getActiveSession(sessionId);

        if (activeSession == null) {
            throw new SessionNotFoundException(sessionId);
        }

        activeSession.close();

        streamingRoom.endSession();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StreamingRoomDTO> getStreamingRoomList() {
        List<StreamingRoom> streamingRooms = streamingRepository.findAllWithMember();

        return streamingRooms.stream()
                .map(room -> {
                    Member member = room.getMember();
                    return new StreamingRoomDTO(
                            room.getId(),
                            room.getSessionId(),
                            room.getTitle(),
                            room.getLatitude(),
                            room.getLongitude(),
                            member.getId(),
                            member.getName()
                    );
                })
                .toList();
    }
}
