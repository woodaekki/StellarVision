package com.susang.stellarVision.application.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileVisibilityUpdateRequest {

    private boolean isGalleryPublic;
    private boolean isVideoPublic;
    private boolean isCollectionPublic;
}
