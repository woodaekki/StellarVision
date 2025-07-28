package com.susang.stellarVision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_s3_key", nullable = false, unique = true, length = 20)
    private String profileS3Key;

    @Column(length = 255)
    private String description;

    @Column(name = "is_gallery_public", nullable = false)
    private Boolean isGalleryPublic;

    @Column(name = "is_video_public", nullable = false)
    private Boolean isVideoPublic;

    @Column(name = "is_collection_public", nullable = false)
    private Boolean isCollectionPublic;

    @Builder
    public Profile(String profileS3Key, String description,
            Boolean isGalleryPublic, Boolean isVideoPublic, Boolean isCollectionPublic) {
        this.profileS3Key = profileS3Key;
        this.description = description;
        this.isGalleryPublic = isGalleryPublic;
        this.isVideoPublic = isVideoPublic;
        this.isCollectionPublic = isCollectionPublic;
    }
}