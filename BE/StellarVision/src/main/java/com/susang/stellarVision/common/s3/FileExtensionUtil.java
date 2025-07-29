package com.susang.stellarVision.common.s3;

public class FileExtensionUtil {

    public static String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot == -1 || dot == filename.length() - 1) {
            throw new IllegalArgumentException("확장자를 찾을 수 없습니다: " + filename);
        }
        return filename.substring(dot).toLowerCase();
    }
}
