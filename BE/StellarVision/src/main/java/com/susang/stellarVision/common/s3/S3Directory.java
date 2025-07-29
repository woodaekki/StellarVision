package com.susang.stellarVision.common.s3;

import lombok.NoArgsConstructor;

public enum S3Directory {
    PROFILE("profile"), GALLERY("gallery"), VIDEO("video"), THUMBNAIL("thumbnail");

    private final String dir;

    S3Directory(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
}