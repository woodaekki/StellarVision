package com.susang.stellarVision.application.profile.service;

public interface ProfileService {

    void saveProfileImageMeta(Long memberId, String originalFilename, String s3Key);

    public String getProfileImage(Long photoId);
}
