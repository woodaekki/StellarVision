package com.susang.stellarVision.application.photo.repository;


import com.susang.stellarVision.entity.Photo;
import com.susang.stellarVision.entity.PhotoTag;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoTagRepository extends JpaRepository<PhotoTag, Long> {

    List<PhotoTag> findAllByPhoto(Photo photo);
}
