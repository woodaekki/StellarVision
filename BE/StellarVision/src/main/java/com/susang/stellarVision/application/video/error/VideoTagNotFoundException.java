package com.susang.stellarVision.application.video.error;

import com.susang.stellarVision.exception.NotFoundException;

public class VideoTagNotFoundException extends NotFoundException {
    public VideoTagNotFoundException(String id) {
        super("VideoTag",id);
    }
}
