package com.susang.stellarVision.application.streaming.service;

import io.openvidu.java.client.*;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StreamingServiceImpl implements StreamingService {

    private final OpenVidu openVidu;


    @Override
    public String createSession(Map<String, Object> params) throws OpenViduJavaClientException, OpenViduHttpException {

        if (params != null && "MEDIA_SERVER_PREFERRED".equals(params.get("forcedVideoCodec"))) {
            params.put("forcedVideoCodec", "VP8");
        }

        SessionProperties properties = SessionProperties.fromJson(params)
                .build();

        Session session = openVidu.createSession(properties);
        return session.getSessionId();
    }
}
