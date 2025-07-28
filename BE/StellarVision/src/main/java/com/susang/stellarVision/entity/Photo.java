package com.susang.stellarVision.entity;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

@Entity
@Table(name = "photos", uniqueConstraints = {
        @UniqueConstraint(name = "UK_PHOTO_S3_KEY", columnNames = {"photo_s3_key"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)

public class Photo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_s3_key", nullable = false, length = 255)
    private String photoS3Key;

    @Column(name = "title", nullable = false, length = 30)
    private String title;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Builder
    public Photo(String photoS3Key, String title, String fileExtension, Member member) {
        this.photoS3Key = photoS3Key;
        this.title = title;
        this.fileExtension = fileExtension;
        this.member = member;
    }
}