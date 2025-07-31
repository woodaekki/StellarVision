package com.susang.stellarVision.application.video.service;


import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import com.susang.stellarVision.application.video.repository.VideoRepository;
import com.susang.stellarVision.entity.Photo;
import com.susang.stellarVision.entity.Thumbnail;
import com.susang.stellarVision.entity.Video;
import com.susang.stellarVision.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
    private final VideoRepository videoRepository;
    private final S3FileManager s3FileManager;

    public String getVideoPresignedUrl(Long videoId) throws VideoNotFoundException {
        return videoRepository.findById(videoId)
                .map(video -> s3FileManager.getPresignedDownloadUrl(video.getVideoS3Key()))
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()) {
                });
    }

    @Override
    @Transactional
    public void deleteVideo(Long videoId) throws VideoNotFoundException {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()) {
                });
        s3FileManager.delete(video.getVideoS3Key());
        videoRepository.delete(video);
    }

    @Override
    public Page<VideoResponse> getVideosByMemberId(Long memberId, Pageable pageable) {
        Page<Video> videos = videoRepository.findByMemberId(memberId, pageable);

        return videos.map(video -> VideoResponse.builder().id(video.getId())
                .id(video.getId())
                .originalFilename(video.getTitle())
                .createdAt(video.getCreatedAt())
                .thumbnailDownloadUrl(s3FileManager.getPresignedDownloadUrl(video.getThumbnail().getThumbnailS3Key())).build());
    }

}
