package com.susang.stellarVision.application.follow.exception;

import com.susang.stellarVision.common.exception.NotFoundException;

public class FollowNotFoundException extends NotFoundException {

    public FollowNotFoundException(String id) {
        super("Follow", id);
    }
}
