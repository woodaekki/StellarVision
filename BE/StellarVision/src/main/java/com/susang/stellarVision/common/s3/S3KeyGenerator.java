package com.susang.stellarVision.common.s3;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class S3KeyGenerator {

    public String generateKey(S3Directory directory, Long memberId, String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = FileExtensionUtil.extractExtension(originalFilename);
        return String.format("%s/%d/%s%s", directory.getDir(), memberId, uuid, extension);
    }
}
