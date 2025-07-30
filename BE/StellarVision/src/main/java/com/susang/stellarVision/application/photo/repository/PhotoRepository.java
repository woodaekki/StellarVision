package com.susang.stellarVision.application.photo.repository;

import com.susang.stellarVision.entity.Photo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    Page<Photo> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByPhotoS3Key(String photoS3Key);
}
