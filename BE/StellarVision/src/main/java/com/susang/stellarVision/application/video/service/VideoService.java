package com.susang.stellarVision.application.video.service;



import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.application.video.dto.VideoTagListResponse;
import com.susang.stellarVision.application.video.dto.VideoTagRequest;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import com.susang.stellarVision.application.video.error.VideoTagNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface VideoService {

    String getVideoPresignedUrl(Long videoId) throws VideoNotFoundException;
    void deleteVideo(Long videoId) throws VideoNotFoundException;
    Page<VideoResponse> getVideosByMemberId(Long memberId, Pageable pageable);
    void addVideoTag(Long videoId,VideoTagRequest videoTagRequest) throws VideoNotFoundException;
    VideoTagListResponse getTagsByVideoId(Long videoId) throws VideoNotFoundException;
    void deleteVideoTag(Long videoId, Long tagId) throws VideoNotFoundException , VideoTagNotFoundException;
}
