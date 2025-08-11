package com.susang.stellarVision.application.video.service;


import com.susang.stellarVision.application.collection.repository.CollectionRepository;
import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.video.dto.*;
import com.susang.stellarVision.application.video.error.DuplicatedVideoTagNameException;
import com.susang.stellarVision.application.video.error.VideoTagMismatchException;
import com.susang.stellarVision.application.video.error.VideoTagNotFoundException;
import com.susang.stellarVision.application.video.error.VideoUploadFailException;
import com.susang.stellarVision.application.video.repository.ThumbnailRepository;
import com.susang.stellarVision.application.video.repository.VideoLikeRepository;
import com.susang.stellarVision.application.video.repository.VideoTagRepository;
import com.susang.stellarVision.common.s3.ContentTypeMapper;
import com.susang.stellarVision.common.s3.FileExtensionUtil;
import com.susang.stellarVision.common.s3.S3Directory;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import com.susang.stellarVision.application.video.repository.VideoRepository;
import com.susang.stellarVision.common.s3.S3KeyGenerator;
import com.susang.stellarVision.entity.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final S3KeyGenerator s3KeyGenerator;
    private final MemberRepository memberRepository;
    private final ThumbnailRepository thumbnailRepository;
    private static final String DEFAULT_THUMBNAIL_KEY = "thumbnail/thumbnail.jpg";
    private final VideoLikeRepository videoLikeRepository;
    private final CollectionRepository collectionRepository;

    private Page<VideoResponse> mapWithLiked(Page<Video> page, Long currentMemberId) {
        List<Long> ids = page.stream().map(Video::getId).toList();
        Set<Long> likedIds = (currentMemberId == null || ids.isEmpty())
                ? Set.of()
                : videoLikeRepository.findLikedVideoIds(currentMemberId, ids);

        return page.map(v -> VideoResponse.builder().id(v.getId())
                .originalFilename(v.getTitle()).createdAt(v.getCreatedAt())
                .thumbnailDownloadUrl(getThumbnailUrl(v.getThumbnail() != null ? v.getThumbnail().getThumbnailS3Key() : null))
                .memberId(v.getMember().getId())
                .nickname(v.getMember().getName())
                .likeCount(v.getLikeCount())
                .liked(currentMemberId != null && likedIds.contains(v.getId()))
                .build());
    }

    private String toThumbnailKeyFromVideoKey(String videoKey) {
        // video/123/abc.mp4  ->  thumbnail/123/abc.jpg
        String noExt = videoKey.substring(0, videoKey.lastIndexOf('.')); // video/123/abc
        return noExt.replaceFirst("^video/", "thumbnail/") + ".jpg";
    }

    private String getThumbnailUrl(String thumbnailS3Key) {
        String key = (thumbnailS3Key == null || thumbnailS3Key.isBlank())
                ? DEFAULT_THUMBNAIL_KEY
                : thumbnailS3Key;

        try {
            if (!s3FileManager.exists(key)) {
                key = DEFAULT_THUMBNAIL_KEY;
            }
            return s3FileManager.getPresignedDownloadUrl(key);
        } catch (Exception e) {
            return s3FileManager.getPresignedDownloadUrl(DEFAULT_THUMBNAIL_KEY);
        }
    }
    public String getVideoPresignedUrl(Long videoId) {
        return videoRepository.findById(videoId)
                .map(video -> s3FileManager.getPresignedDownloadUrl(video.getVideoS3Key()))
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()) {
                });
    }




    @Override
    @Transactional(readOnly = true)
    public Page<VideoResponse> getVideosByMemberId(Long memberId, Pageable pageable, Long currentMemberId) {
        Page<Video> page = videoRepository.findByMemberId(memberId, pageable);
        return mapWithLiked(page, currentMemberId);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoResponse> getVideos(Pageable pageable, Long memberId) {
        Page<Video> page = videoRepository.findAll(pageable);
        return mapWithLiked(page, memberId);

    }

    @Override
    @Transactional(readOnly = true)
    public Page<VideoResponse> searchVideos(VideoSearchRequest condition, Pageable pageable, Long memberId) {
        Page<Video> page = videoRepository.search(condition, pageable);
        return mapWithLiked(page, memberId);
    }

    @Override
    public void addVideoTag(Long videoId, VideoTagRequest request) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));

        boolean exists = videoTagRepository.existsByVideoAndTagName(video, request.getTagName());
        if (exists) {
            throw new DuplicatedVideoTagNameException("이미 해당 태그가 존재합니다.");
        }

        VideoTag tag = VideoTag.builder().tagName(request.getTagName()).video(video).build();

        videoTagRepository.save(tag);
    }
    @Override
    @Transactional
    public void addAiVideoTags(Long videoId, List<String> tags) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));
        List<Collection> allCollections = collectionRepository.findAll();
        for (String tag : tags) {
            String koreanName = allCollections.stream()
                    .filter(c -> c.getAbbreviation().equals(tag))
                    .map(Collection::getKoreanName)
                    .findFirst()
                    .orElse(null);

            String tagName = (koreanName != null) ? koreanName : tag;

            VideoTag videoTag = VideoTag.builder()
                    .tagName(tagName)
                    .video(video)
                    .build();

            videoTagRepository.save(videoTag);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public VideoTagListResponse getTagsByVideoId(Long videoId) {
        if (!videoRepository.existsById(videoId)) {
            throw new VideoNotFoundException(videoId.toString());
        }
        List<VideoTag> tags = videoTagRepository.findAllByVideoId(videoId);
        List<VideoTagResponse> tagList = tags.stream()
                .map(tag -> new VideoTagResponse(tag.getId(), tag.getTagName())).toList();

        return new VideoTagListResponse(tagList);
    }

    @Override
    @Transactional
    public void deleteVideoTag(Long videoId, Long tagId) {
        VideoTag tag = videoTagRepository.findById(tagId)
                .orElseThrow(() -> new VideoTagNotFoundException(tagId.toString()));

        if (!tag.getVideo().getId().equals(videoId)) {
            throw new VideoTagMismatchException("해당 태그는 요청한 비디오에 속하지 않습니다");
        }
        videoTagRepository.delete(tag);
    }

    @Override
    @Transactional
    public void updateVideoContent(Long videoId, VideoUpdateRequest request) {

        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));

        videoTagRepository.deleteAllByVideoId(videoId);

        video.updateTitle(request.getTitle());

        List<VideoTag> tags = request.getTags().stream()
                .map(tagName -> new VideoTag(tagName.getTagName(), video)).toList();

        videoTagRepository.saveAll(tags);
    }

    @Override
    @Transactional
    public Long uploadVideo(InputStream inputStream, Long contentLength, String title,
            Long memberId) {
        String originalFilename = title + ".mp4";
        String extension = FileExtensionUtil.extractExtension(originalFilename);
        String contentType = ContentTypeMapper.fromExtension(extension);
        String s3Key = s3KeyGenerator.generateKey(S3Directory.VIDEO, memberId, originalFilename);

        try {
            s3FileManager.uploadFile(inputStream, contentLength, contentType, s3Key);
        } catch (IOException e) {
            throw new VideoUploadFailException("S3 영상 업로드 실패");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));

        String thumbnailKey = toThumbnailKeyFromVideoKey(s3Key);

        Thumbnail thumbnail = Thumbnail.builder()
                .thumbnailS3Key(thumbnailKey)
                .build();
        thumbnailRepository.save(thumbnail);


        Video video = Video.builder().videoS3Key(s3Key).title(title).member(member)
                .thumbnail(thumbnail).build();
        videoRepository.save(video);
        return video.getId();
    }

    @Override
    @Transactional
    public VideoLikeResponse like(Long videoId, Long memberId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));

        if (videoLikeRepository.existsByVideoIdAndMemberId(videoId, memberId)) {
            long current = videoRepository.getLikeCount(videoId);
            return VideoLikeResponse.builder().videoId(videoId).liked(true).likeCount(current).build();
        }

        try {
            videoLikeRepository.save(VideoLike.builder().video(video)
                    .member(member).build());

            videoRepository.updateLikeCount (videoId, +1);
            long current = videoRepository.getLikeCount(videoId);
            return VideoLikeResponse.builder().videoId(videoId).liked(true).likeCount(current).build();
        } catch (DataIntegrityViolationException e) {
            long current = videoRepository.getLikeCount(videoId);
            return VideoLikeResponse.builder().videoId(videoId).liked(true).likeCount(current).build();
        }
    }

    @Override
    @Transactional
    public VideoLikeResponse unlike(Long videoId, Long memberId) {
        videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));

        long deleted = videoLikeRepository.deleteByVideoIdAndMemberId(videoId, memberId);
        if (deleted > 0) {
            videoRepository.updateLikeCount (videoId, -1);
        }
        long current = videoRepository.getLikeCount(videoId);
        return VideoLikeResponse.builder().videoId(videoId).liked(false).likeCount(current).build();
    }


    @Override
    @Transactional
    public void deleteVideo(Long videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new VideoNotFoundException(videoId.toString()));

        s3FileManager.delete(video.getVideoS3Key());

        String thumbnailKey = (video.getThumbnail() != null)
                ? video.getThumbnail().getThumbnailS3Key()
                : null;
        if (thumbnailKey != null && !thumbnailKey.isBlank() && !DEFAULT_THUMBNAIL_KEY.equals(thumbnailKey)) {
            s3FileManager.delete(thumbnailKey);
        }

        videoLikeRepository.deleteAllByVideoId(videoId);
        videoRepository.delete(video);
    }

}
