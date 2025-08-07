package com.susang.stellarVision.application.follow.exception;

import com.susang.stellarVision.common.exception.AlreadyExistException;

public class AlreadyFollowException extends AlreadyExistException {

    public AlreadyFollowException(String message) {
        super("Follower", message);
    }
}
