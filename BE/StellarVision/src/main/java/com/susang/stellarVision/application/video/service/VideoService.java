package com.susang.stellarVision.application.video.service;


import com.susang.stellarVision.application.video.dto.*;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import com.susang.stellarVision.application.video.error.VideoTagNotFoundException;
import com.susang.stellarVision.application.video.error.VideoUploadFailException;
import java.io.InputStream;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface VideoService {

    String getVideoPresignedUrl(Long videoId);

    void deleteVideo(Long videoId);

    Page<VideoResponse> getVideosByMemberId(Long memberId, Pageable pageable, Long currentMemberId);

    Page<VideoResponse> getVideos(Pageable pageable, Long memberId);

    Page<VideoResponse> searchVideos(VideoSearchRequest condition, Pageable pageable,
            Long memberId);

    void addVideoTag(Long videoId, VideoTagRequest videoTagRequest);

    void addAiVideoTags(Long videoId, List<String> tags);

    VideoTagListResponse getTagsByVideoId(Long videoId);

    void deleteVideoTag(Long videoId, Long tagId);

    void updateVideoContent(Long videoId, VideoUpdateRequest videoUpdateRequest);

    Long uploadVideo(InputStream inputStream, Long contentLength, String title, Long memberId);

    VideoLikeResponse like(Long videoId, Long memberId);

    VideoLikeResponse unlike(Long videoId, Long memberId);
}
