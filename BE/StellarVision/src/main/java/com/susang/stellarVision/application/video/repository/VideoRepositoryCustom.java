package com.susang.stellarVision.application.video.repository;


import com.susang.stellarVision.application.video.dto.VideoSearchRequest;
import com.susang.stellarVision.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VideoRepositoryCustom {
    Page<Video> search(VideoSearchRequest condition, Pageable pageable);
}

