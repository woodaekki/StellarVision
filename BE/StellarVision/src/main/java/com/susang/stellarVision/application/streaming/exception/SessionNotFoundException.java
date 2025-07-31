package com.susang.stellarVision.application.streaming.exception;

import com.susang.stellarVision.exception.NotFoundException;

public class SessionNotFoundException extends NotFoundException {

    public SessionNotFoundException(String id) {
        super("Session", id);
    }
}
