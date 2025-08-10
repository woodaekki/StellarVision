package com.susang.stellarVision.application.video.repository;

import com.susang.stellarVision.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {

    long deleteByVideoIdAndMemberId(Long videoId, Long memberId);

    boolean existsByVideoIdAndMemberId(Long videoId, Long memberId);

    @Query("SELECT vl.video.id FROM VideoLike vl WHERE vl.member.id=:memberId AND vl.video.id IN :videoIds")
    Set<Long> findLikedVideoIds(@Param("memberId") Long memberId, @Param("videoIds") Collection<Long> videoIds);

    long deleteAllByVideoId(Long videoId);


}