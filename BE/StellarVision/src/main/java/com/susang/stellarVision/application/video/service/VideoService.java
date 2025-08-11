package com.susang.stellarVision.application.video.service;



import com.susang.stellarVision.application.video.dto.*;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import com.susang.stellarVision.application.video.error.VideoTagNotFoundException;
import com.susang.stellarVision.application.video.error.VideoUploadFailException;
import java.io.InputStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface VideoService {

    String getVideoPresignedUrl(Long videoId) throws VideoNotFoundException;
    void deleteVideo(Long videoId) throws VideoNotFoundException;
    Page<VideoResponse> getVideosByMemberId(Long memberId, Pageable pageable,Long currentMemberId);
    Page<VideoResponse> getVideos(Pageable pageable ,Long memberId);
    Page<VideoResponse> searchVideos(VideoSearchRequest condition, Pageable pageable, Long memberId);
    void addVideoTag(Long videoId,VideoTagRequest videoTagRequest) throws VideoNotFoundException;
    VideoTagListResponse getTagsByVideoId(Long videoId) throws VideoNotFoundException;
    void deleteVideoTag(Long videoId, Long tagId) throws VideoNotFoundException , VideoTagNotFoundException;
    void updateVideoContent(Long videoId,VideoUpdateRequest videoUpdateRequest) throws VideoNotFoundException;
    Long uploadVideo(InputStream inputStream,Long contentLength, String title, Long memberId) throws VideoUploadFailException;
    VideoLikeResponse like(Long videoId, Long memberId);
    VideoLikeResponse unlike(Long videoId, Long memberId);
}
