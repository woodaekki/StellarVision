package com.susang.stellarVision.application.auth.email.exception;

import com.susang.stellarVision.common.exception.BadRequestException;
import com.susang.stellarVision.common.exception.NotFoundException;

public class CodeNotFoundException extends BadRequestException {

    public CodeNotFoundException(String email) {
        super("Email-Code", email);
    }
}
