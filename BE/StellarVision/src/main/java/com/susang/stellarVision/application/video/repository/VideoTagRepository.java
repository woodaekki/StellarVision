package com.susang.stellarVision.application.video.repository;

import com.susang.stellarVision.entity.Video;
import com.susang.stellarVision.entity.VideoTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoTagRepository extends JpaRepository<VideoTag, Long> {

    boolean existsByVideoAndTagName(Video video, String tagName);

    List<VideoTag> findAllByVideoId(Long videoId);

    void deleteByIdAndVideoId(Long videoId,Long tagId);

    void deleteAllByVideoId(Long videoId);
}
