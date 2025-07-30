package com.susang.stellarVision.application.photo.error;

public class S3DeletionFailedException extends RuntimeException {

    public S3DeletionFailedException(String message) {
        super(message);
    }
}
