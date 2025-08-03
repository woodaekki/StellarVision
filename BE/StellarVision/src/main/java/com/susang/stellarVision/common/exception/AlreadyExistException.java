package com.susang.stellarVision.common.exception;

public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String resourceName, String id) {
      super(String.format("%s(%s)는 이미 존재합니다", resourceName, id));
    }
}
