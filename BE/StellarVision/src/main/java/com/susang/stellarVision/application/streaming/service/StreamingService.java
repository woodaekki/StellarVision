package com.susang.stellarVision.application.streaming.service;

import io.openvidu.java.client.OpenViduException;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import java.util.Map;

public interface StreamingService {

    String createSession(Map<String, Object> params)
            throws OpenViduJavaClientException, OpenViduHttpException;
}
