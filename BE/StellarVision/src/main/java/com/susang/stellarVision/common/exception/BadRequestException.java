package com.susang.stellarVision.common.exception;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String resourceName, String id) {
        super(String.format("%s(%s)에 대한 잘못된 요청입니다.", resourceName, id));
    }
}
