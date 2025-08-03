package com.susang.stellarVision.application.video.error;

import com.susang.stellarVision.common.exception.NotFoundException;

public class VideoTagNotFoundException extends NotFoundException {
    public VideoTagNotFoundException(String id) {
        super("VideoTag",id);
    }
}
