package com.susang.stellarVision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_PROFILE_KEY",
                        columnNames = {"profile_s3_key"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "profile_s3_key", length = 255)
    private String profileS3Key;

    @Column
    private String description;

    @Setter
    @Column(name = "is_gallery_public", nullable = false)
    private boolean isGalleryPublic;

    @Setter
    @Column(name = "is_video_public", nullable = false)
    private boolean isVideoPublic;

    @Setter
    @Column(name = "is_collection_public", nullable = false)
    private boolean isCollectionPublic;

    @Builder
    public Profile(String profileS3Key, String description) {
        this.profileS3Key = profileS3Key;
        this.description = description;
        this.isGalleryPublic = true;
        this.isVideoPublic = true;
        this.isCollectionPublic = true;
    }
}