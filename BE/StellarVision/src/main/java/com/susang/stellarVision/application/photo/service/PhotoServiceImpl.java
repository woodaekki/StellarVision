package com.susang.stellarVision.application.photo.service;

import com.susang.stellarVision.common.s3.ContentTypeMapper;
import com.susang.stellarVision.common.s3.FileExtensionUtil;
import com.susang.stellarVision.common.s3.S3Directory;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.common.s3.S3KeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private final S3FileManager s3FileManager;
    private final S3KeyGenerator s3KeyGenerator;

    @Override
    public String getPresignedUploadUrl(Long memberId, String originalFilename) {

        String key = s3KeyGenerator.generateKey(S3Directory.GALLERY, memberId, originalFilename);

        String extension = FileExtensionUtil.extractExtension(originalFilename);
        String contentType = ContentTypeMapper.fromExtension(extension);

        return s3FileManager.getPresignedUploadUrl(key, contentType);
    }

    @Override
    public String getPresignedDownloadUrl(String photoS3Key) {

        return s3FileManager.getPresignedDownloadUrl(photoS3Key);
    }

}
