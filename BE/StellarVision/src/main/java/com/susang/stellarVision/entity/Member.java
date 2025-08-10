package com.susang.stellarVision.entity;

import com.susang.stellarVision.application.member.dto.AuthProvider;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.SQLSelect;

@Entity
@Table(
        name = "members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_MEMBER_EMAIL",
                        columnNames = {"email"}
                )
        }
)
@Getter
@SQLDelete(sql = "UPDATE members SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@SQLSelect(sql = "SELECT * FROM members WHERE is_deleted = false AND id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 225)
    private String email;

    @Column(length = 60)
    private String password;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "latest_login")
    private LocalDateTime latestLogin;

    @Column(name = "follower_count", nullable = false)
    private Long followerCount;

    @Column(name = "following_count", nullable = false)
    private Long followingCount;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private AuthProvider provider;

    @Column(name = "provider_id")
    private String providerId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @Builder
    public Member(String email, String password, String name, LocalDate birth,
            LocalDateTime latestLogin, Long followerCount,
            Long followingCount, Boolean isDeleted, AuthProvider provider, String providerId,
            Profile profile) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birth = birth;
        this.latestLogin = latestLogin;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.isDeleted = isDeleted;
        this.provider = provider;
        this.providerId = providerId;
        this.profile = profile;
    }

    public void updateLatestLogin() {
        this.latestLogin = LocalDateTime.now();
    }

    public void decreaseFollowerCount() {
        this.followerCount--;
    }

    public void decreaseFollowingCount() {
        this.followingCount--;
    }

    public void increaseFollowerCount() {
        this.followerCount++;
    }

    public void increaseFollowingCount() {
        this.followingCount++;
    }

    public void linkProvider(AuthProvider provider, String providerUserId) {
        this.provider = provider;
        this.providerId = providerUserId;
    }
}
