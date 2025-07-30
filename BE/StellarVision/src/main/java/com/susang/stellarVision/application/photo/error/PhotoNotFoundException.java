package com.susang.stellarVision.application.photo.error;

public class PhotoNotFoundException extends RuntimeException {
    public PhotoNotFoundException(Long photoId) {
        super("존재하지 않는 사진입니다. id=" + photoId);
    }
}