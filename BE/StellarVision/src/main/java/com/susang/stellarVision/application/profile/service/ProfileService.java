package com.susang.stellarVision.application.profile.service;

import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.photo.error.S3DeletionFailedException;

public interface ProfileService {

    void saveProfileImageMeta(Long memberId, String originalFilename, String s3Key) throws S3DeletionFailedException, MemberNotFoundException;

    String getProfileImage(Long photoId) throws MemberNotFoundException;
}
