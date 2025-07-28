package com.susang.stellarVision.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table (name = "video_tags")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VideoTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", nullable = false, length = 30)
    private String tagName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Builder
    public VideoTag(String tagName, Video video) {
        this.tagName = tagName;
        this.video = video;
    }
}
