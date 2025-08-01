package com.susang.stellarVision.application.video.service;


import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.application.video.dto.VideoTagListResponse;
import com.susang.stellarVision.application.video.dto.VideoTagRequest;
import com.susang.stellarVision.application.video.dto.VideoTagResponse;
import com.susang.stellarVision.application.video.error.DuplicatedVideoTagNameException;
import com.susang.stellarVision.application.video.error.VideoTagMismatchException;
import com.susang.stellarVision.application.video.error.VideoTagNotFoundException;
import com.susang.stellarVision.application.video.repository.VideoTagRepository;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import com.susang.stellarVision.application.video.repository.VideoRepository;
import com.susang.stellarVision.entity.Photo;
import com.susang.stellarVision.entity.Thumbnail;
import com.susang.stellarVision.entity.Video;
import com.susang.stellarVision.entity.VideoTag;
import com.susang.stellarVision.exception.NotFoundException;
import java.util.List;
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
    private final VideoTagRepository videoTagRepository;

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

        return videos.map(video -> VideoResponse.builder().id(video.getId()).id(video.getId())
                .originalFilename(video.getTitle()).createdAt(video.getCreatedAt())
                .thumbnailDownloadUrl(s3FileManager.getPresignedDownloadUrl(
                        video.getThumbnail().getThumbnailS3Key())).build());
    }

    @Override
    public void addVideoTag(Long videoId,VideoTagRequest request) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException("해당 영상이 존재하지 않습니다."));

        boolean exists = videoTagRepository.existsByVideoAndTagName(video, request.getTagName());
        if (exists) {
            throw new DuplicatedVideoTagNameException("이미 해당 태그가 존재합니다.");
        }

        VideoTag tag = VideoTag.builder().tagName(request.getTagName()).video(video).build();

        videoTagRepository.save(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public VideoTagListResponse getTagsByVideoId(Long videoId) {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException("해당 영상이 존재하지 않습니다.");
        }
        List<VideoTag> tags = videoTagRepository.findAllByVideoId(videoId);
        List<VideoTagResponse> tagList = tags.stream()
                .map(tag -> new VideoTagResponse(tag.getId(), tag.getTagName())).toList();

        return new VideoTagListResponse(tagList);
    }

    @Override
    @Transactional
    public void deleteVideoTag(Long videoId, Long tagId)
            throws VideoTagNotFoundException {
        VideoTag tag = videoTagRepository.findById(tagId)
                .orElseThrow(() -> new VideoTagNotFoundException(tagId.toString()));

        if (!tag.getVideo().getId().equals(videoId)) {
            throw new VideoTagMismatchException("해당 태그는 요청한 비디오에 속하지 않습니다");
        }
        videoTagRepository.delete(tag);
    }


}
