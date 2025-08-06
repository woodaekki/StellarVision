package com.susang.stellarVision.application.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileResponse {

    private Long memberId;
    private String profileImageUrl;
    private String description;
    private boolean isGalleryPublic;
    private boolean isVideoPublic;
    private boolean isCollectionPublic;
    private Long followerCount;
    private Long followingCount;
}

