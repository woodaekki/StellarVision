package com.susang.stellarVision.common.s3;


import com.susang.stellarVision.config.S3Config;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class S3FileManager {

    private final S3Config config;
    private S3Presigner presigner;
    private S3Client s3Client;

    @PostConstruct
    private void init() {
        this.presigner = createPresigner();
        this.s3Client = createS3Client();
    }

    private S3Presigner createPresigner() {
        return S3Presigner.builder().region(Region.of(config.getRegion())).credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())))
                .build();
    }

    private S3Client createS3Client() {
        return S3Client.builder().region(Region.of(config.getRegion())).credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(config.getAccessKey(), config.getSecretKey())))
                .build();
    }

    // URL 유효 시간 설정
    private static final Duration UPLOAD_URL_DURATION = Duration.ofMinutes(10);
    private static final Duration DOWNLOAD_URL_DURATION = Duration.ofMinutes(5);

    public String getPresignedUploadUrl(String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder().bucket(config.getBucket()).key(key)
                .contentType(contentType).build();

        PutObjectPresignRequest presign = PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_DURATION).putObjectRequest(request).build();

        return presigner.presignPutObject(presign).url().toString();
    }

    public String getPresignedDownloadUrl(String key) {
        GetObjectRequest request = GetObjectRequest.builder().bucket(config.getBucket()).key(key)
                .build();

        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(DOWNLOAD_URL_DURATION).getObjectRequest(request).build();

        return presigner.presignGetObject(presign).url().toString();
    }

    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder().bucket(config.getBucket())
                .key(key).build();

        s3Client.deleteObject(request);
    }
    public void uploadFile(InputStream inputStream, long contentLength, String contentType, String key) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
    }
}
