package com.susang.stellarVision.application.video.service;



import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.application.video.dto.VideoSearchRequest;
import com.susang.stellarVision.application.video.dto.VideoTagListResponse;
import com.susang.stellarVision.application.video.dto.VideoTagRequest;
import com.susang.stellarVision.application.video.dto.VideoUpdateRequest;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import com.susang.stellarVision.application.video.error.VideoTagNotFoundException;
import com.susang.stellarVision.application.video.error.VideoUploadFailException;
import java.io.InputStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface VideoService {

    String getVideoPresignedUrl(Long videoId) throws VideoNotFoundException;
    void deleteVideo(Long videoId) throws VideoNotFoundException;
    Page<VideoResponse> getVideosByMemberId(Long memberId, Pageable pageable);
    Page<VideoResponse> getVideos(Pageable pageable);
    Page<VideoResponse> searchVideos(VideoSearchRequest condition, Pageable pageable);
    void addVideoTag(Long videoId,VideoTagRequest videoTagRequest) throws VideoNotFoundException;
    VideoTagListResponse getTagsByVideoId(Long videoId) throws VideoNotFoundException;
    void deleteVideoTag(Long videoId, Long tagId) throws VideoNotFoundException , VideoTagNotFoundException;
    void updateVideoContent(Long videoId,VideoUpdateRequest videoUpdateRequest) throws VideoNotFoundException;
    void uploadVideo(InputStream inputStream,Long cententLength, String title, Long memberId) throws VideoUploadFailException;
}
