package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.streaming.dto.ConnectionTokenDTO;
import com.susang.stellarVision.application.streaming.dto.CreateStreamingSessionRequest;
import com.susang.stellarVision.application.streaming.dto.RecordingInfoDTO;
import com.susang.stellarVision.application.streaming.dto.StreamingRoomDTO;
import com.susang.stellarVision.application.streaming.exception.AccessDeniedException;
import com.susang.stellarVision.application.streaming.exception.RecordingNotFoundException;
import com.susang.stellarVision.application.streaming.exception.SessionNotFoundException;
import com.susang.stellarVision.application.streaming.repository.StreamingRepository;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.StreamingRoom;
import io.openvidu.java.client.*;

import java.util.List;
import io.openvidu.java.client.Recording.OutputMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        SessionProperties.Builder builder = new SessionProperties.Builder()
                .forcedVideoCodec(VideoCodec.VP8)
                .mediaMode(MediaMode.ROUTED)
                .recordingMode(RecordingMode.MANUAL);

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
    public ConnectionTokenDTO createToken(String sessionId, Member member)
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

        String token = connection.getToken();

        return new  ConnectionTokenDTO(token, role.name());
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

    @Override
    @Transactional
    public String startRecording(String sessionId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException {

        StreamingRoom streamingRoom = streamingRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!member.getId()
                .equals(streamingRoom.getMember()
                        .getId())) {
            throw new AccessDeniedException("생성한 멤버만 녹화를 시작할 수 있습니다.");
        }

        Session activeSession = openVidu.getActiveSession(sessionId);
        if (activeSession == null) {
            throw new SessionNotFoundException(sessionId);
        }

        RecordingProperties defaultProps = activeSession.getProperties()
                .defaultRecordingProperties();

        RecordingProperties props = new RecordingProperties.Builder(defaultProps)
                .name(streamingRoom.getTitle())
                .outputMode(OutputMode.COMPOSED)
                .build();

        Recording recording = openVidu.startRecording(sessionId, props);

        streamingRoom.markRecordingStarted(recording.getId());

        return recording.getId();
    }

    @Override
    @Transactional
    public RecordingInfoDTO stopRecording(String recordingId, Member member)
            throws OpenViduJavaClientException, OpenViduHttpException {
        StreamingRoom streamingRoom = streamingRepository.findByRecordingId(recordingId)
                .orElseThrow(() -> new RecordingNotFoundException(recordingId));

        if (!member.getId()
                .equals(streamingRoom.getMember()
                        .getId())) {
            throw new AccessDeniedException("생성한 멤버만 녹화를 중지할 수 있습니다.");
        }

        Recording recording = openVidu.stopRecording(recordingId);

        Instant created = Instant.ofEpochMilli(recording.getCreatedAt());
        streamingRoom.markRecordingStopped(recording.getUrl(),
                LocalDateTime.ofInstant(created, ZoneOffset.UTC));

        return new RecordingInfoDTO(
                recordingId,
                recording.getUrl(),
                recording.getName(),
                LocalDateTime.ofInstant(created, ZoneOffset.UTC)
        );
    }
}
