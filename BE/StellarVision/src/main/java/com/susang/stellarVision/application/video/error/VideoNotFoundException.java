package com.susang.stellarVision.application.video.error;

import com.susang.stellarVision.common.exception.NotFoundException;

public class VideoNotFoundException extends NotFoundException {

    public VideoNotFoundException(String id) {
        super("Video", id);
    }
}
