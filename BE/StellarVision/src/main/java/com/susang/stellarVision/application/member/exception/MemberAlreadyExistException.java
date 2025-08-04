package com.susang.stellarVision.application.member.exception;

import com.susang.stellarVision.common.exception.AlreadyExistException;

public class MemberAlreadyExistException extends AlreadyExistException {
    public MemberAlreadyExistException(String id) {
        super("Member", id);
    }
}
