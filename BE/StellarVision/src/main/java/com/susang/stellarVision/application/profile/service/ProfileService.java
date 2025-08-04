package com.susang.stellarVision.application.profile.service;

import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.photo.error.S3DeletionFailedException;
import com.susang.stellarVision.application.profile.dto.ProfileResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import org.springframework.transaction.annotation.Transactional;


public interface ProfileService {

    void saveProfileImageMeta(Long memberId, String originalFilename, String s3Key) throws S3DeletionFailedException, MemberNotFoundException;

    String getProfileImage(Long photoId) throws MemberNotFoundException;
    @Transactional(readOnly = true)
    ProfileResponse getMyProfileInfo(CustomUserDetails userDetails) throws MemberNotFoundException;

    ProfileResponse getProfileInfo(Long memberId) throws MemberNotFoundException, MemberNotFoundException;
}
