package com.susang.stellarVision.exception;

public abstract class NotFoundException extends RuntimeException {

    public NotFoundException(String resourceName, String id) {
        super(String.format("%s(%s)를 찾을 수 없습니다.", resourceName, id));
    }
}
