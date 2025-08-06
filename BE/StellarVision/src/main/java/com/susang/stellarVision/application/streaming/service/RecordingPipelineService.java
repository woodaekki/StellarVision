package com.susang.stellarVision.application.streaming.service;

import com.susang.stellarVision.application.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordingPipelineService {

    private final RecordingDownloadService downloadService;
    private final VideoService videoService;

    public void downloadAndUploadAsync(String url, String title, Long memberId) {
        downloadService.getRecordingStream(url)
                .thenAccept(stream -> {
                    videoService.uploadVideo(
                            stream.getInputStream(),
                            stream.getContentLength(),
                            title,
                            memberId
                    );
                })
                .exceptionally(ex ->{
                    log.error("다운로드-업로드 파이프라인 오류", ex);
                    return null;
                });
    }
}
