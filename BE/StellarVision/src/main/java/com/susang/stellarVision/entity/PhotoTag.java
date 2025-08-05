package com.susang.stellarVision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table (name = "photo_tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PhotoTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    @ManyToOne
    @JoinColumn(name = "photo_id", nullable = false)
    private Photo photo;

    @Builder
    public PhotoTag(String tagName, Photo photo) {
        this.tagName = tagName;
        this.photo = photo;
    }
}