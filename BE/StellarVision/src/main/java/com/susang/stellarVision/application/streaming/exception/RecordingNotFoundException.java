package com.susang.stellarVision.application.streaming.exception;


import com.susang.stellarVision.common.exception.NotFoundException;

public class RecordingNotFoundException extends NotFoundException {

    public RecordingNotFoundException(String id) {
        super("Record", id);
    }
}
