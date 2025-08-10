package com.susang.stellarVision.application.video.repository;

import com.susang.stellarVision.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, VideoRepositoryCustom {
    Page<Video> findByMemberId(Long memberId, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Video v 
            SET v.likeCount = CASE 
              WHEN :delta < 0 AND v.likeCount + :delta < 0 THEN 0 
              ELSE v.likeCount + :delta 
            END 
            WHERE v.id = :videoId
            """)
    int updateLikeCount (@Param("videoId") Long videoId, @Param("delta") int delta);

    @Query("SELECT v.likeCount FROM Video v WHERE v.id=:videoId")
    long getLikeCount(@Param("videoId") Long videoId);
}