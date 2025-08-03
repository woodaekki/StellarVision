package com.susang.stellarVision.application.member.exception;

import com.susang.stellarVision.common.exception.NotFoundException;

public class MemberNotFoundException extends NotFoundException {

    public MemberNotFoundException(String id) {
        super("Member", id);
    }
}
