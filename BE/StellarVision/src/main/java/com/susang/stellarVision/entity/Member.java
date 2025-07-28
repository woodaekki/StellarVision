package com.susang.stellarVision.entity;

import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 20)
    private String email;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, length = 10)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birth;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "latest_login")
    private LocalDateTime latestLogin;

    @Column(name = "follower_count", nullable = false)
    private Long followerCount;

    @Column(name = "following_count", nullable = false)
    private Long followingCount;

    @Setter
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Builder
    public Member(String email, String password, String nickname, LocalDate birth,
            LocalDateTime createdAt, LocalDateTime latestLogin, Long followerCount,
            Long followingCount, Boolean isDeleted, Profile profile) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.birth = birth;
        this.createdAt = createdAt;
        this.latestLogin = latestLogin;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.isDeleted = isDeleted;
        this.profile = profile;
    }
}
