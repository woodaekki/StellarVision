package com.susang.stellarVision.application.streaming.exception;

public class RecordingInProgressException extends RuntimeException {

    public RecordingInProgressException(String sessionId) {
      super("세션 [" + sessionId + "] 은(는) 아직 녹화 중입니다. 먼저 녹화를 중지해주세요.");
    }
}
