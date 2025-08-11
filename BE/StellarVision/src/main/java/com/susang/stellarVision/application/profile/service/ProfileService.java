package com.susang.stellarVision.application.profile.service;

import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.photo.error.S3DeletionFailedException;
import com.susang.stellarVision.application.profile.dto.ProfileResponse;
import com.susang.stellarVision.application.profile.dto.ProfileVisibilityUpdateRequest;
import com.susang.stellarVision.application.profile.dto.UpdateDescriptionrequest;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;


public interface ProfileService {

    void saveProfileImageMeta(Long memberId, String originalFilename, String s3Key)
            throws S3DeletionFailedException, MemberNotFoundException;

    String getProfileImage(Long photoId);

    ProfileResponse getMyProfileInfo(CustomUserDetails userDetails);

    ProfileResponse getProfileInfo(Long memberId)
            throws MemberNotFoundException, MemberNotFoundException;

    void updateVisibility(CustomUserDetails userDetails,
            ProfileVisibilityUpdateRequest profileVisibilityUpdateRequest);

    String getProfileImageUrl(String s3Key);

    void deleteProfileImage(CustomUserDetails userDetails);

    void updateDescription(CustomUserDetails userDetails, UpdateDescriptionrequest updateDescriptionrequest);

}
