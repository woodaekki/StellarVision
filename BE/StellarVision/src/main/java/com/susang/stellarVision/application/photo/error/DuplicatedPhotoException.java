package com.susang.stellarVision.application.photo.error;

public class DuplicatedPhotoException extends RuntimeException {

    public DuplicatedPhotoException(String s3Key) {
        super("이미 업로드 완료된 사진입니다. (s3Key=" + s3Key + ")");
    }
}
