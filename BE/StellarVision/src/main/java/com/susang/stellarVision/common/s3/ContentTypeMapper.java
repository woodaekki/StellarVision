package com.susang.stellarVision.common.s3;

public class ContentTypeMapper {

    public static String fromExtension(String extension) {
        return switch (extension.toLowerCase()) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            case ".mp4" -> "video/mp4";

            default -> throw new IllegalArgumentException("지원하지 않는 확장자: " + extension);
        };
    }
}
