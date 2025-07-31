package com.susang.stellarVision.application.video.error;

import com.susang.stellarVision.exception.NotFoundException;

public class VideoNotFoundException extends NotFoundException {

    public VideoNotFoundException(String id) {
        super("Video", id);
    }
}
