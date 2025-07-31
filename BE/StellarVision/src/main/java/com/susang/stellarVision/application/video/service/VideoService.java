package com.susang.stellarVision.application.video.service;

import com.susang.stellarVision.application.photo.dto.PhotoResponse;
import com.susang.stellarVision.application.video.dto.VideoResponse;
import com.susang.stellarVision.application.video.error.VideoNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

public interface VideoService {
    public String getVideoPresignedUrl(@PathVariable Long videoId) throws VideoNotFoundException;
    public void deleteVideo(@PathVariable  Long videoId) throws VideoNotFoundException;
    Page<VideoResponse> getVideosByMemberId(Long memberId, Pageable pageable);

}
