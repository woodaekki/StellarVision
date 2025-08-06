package com.susang.stellarVision.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "videos", uniqueConstraints = {
        @UniqueConstraint(name = "UK_VIDEO_S3_KEY", columnNames = {"video_s3_key"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_s3_key", nullable = false, length = 255)
    private String videoS3Key;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "like_count")
    @ColumnDefault("0")
    private Long likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thumbnail_id", nullable = false)
    private Thumbnail thumbnail;

    @Builder
    public Video(String videoS3Key, String title, Long likeCount, Member member,
            Thumbnail thumbnail) {
        this.videoS3Key = videoS3Key;
        this.title = title;
        this.likeCount = likeCount;
        this.member = member;
        this.thumbnail = thumbnail;
    }

    public void updateTitle(String newTitle) {
        this.title = newTitle;
    }
}
