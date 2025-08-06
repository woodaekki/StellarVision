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

@Entity
@Table(name = "thumbnails", uniqueConstraints = {
        @UniqueConstraint(name = "UK_THUMBNAIL_S3_KEY", columnNames = {"thumbnail_s3_key"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Thumbnail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "thumbnail_s3_key", nullable = false)
    private String thumbnailS3Key;

    @Builder
    public Thumbnail(String thumbnailS3Key) {
        this.thumbnailS3Key = "thumbnail/thumbnail.jpg";
    }
    public void updateThumbnailS3Key(String thumbnailS3Key) {
        this.thumbnailS3Key = thumbnailS3Key;
    }
}
