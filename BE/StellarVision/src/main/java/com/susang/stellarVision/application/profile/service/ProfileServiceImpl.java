package com.susang.stellarVision.application.profile.service;

import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.photo.error.S3DeletionFailedException;
import com.susang.stellarVision.application.profile.dto.ProfileResponse;
import com.susang.stellarVision.application.profile.dto.ProfileVisibilityUpdateRequest;
import com.susang.stellarVision.application.profile.dto.UpdateDescriptionrequest;
import com.susang.stellarVision.application.profile.error.DescriptionTooLongException;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.common.s3.S3ResizedImageKeyMapper;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.Profile;
import java.io.IOException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class ProfileServiceImpl implements ProfileService {

    private final MemberRepository memberRepository;
    private final S3FileManager s3FileManager;
    private static final String DEFAULT_PROFILE_IMAGE_KEY = "profile/profile.png";


    @Override
    public String getProfileImageUrl(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return s3FileManager.getPresignedDownloadUrl(DEFAULT_PROFILE_IMAGE_KEY);
        }
        return s3FileManager.getPresignedDownloadUrl(s3Key);
    }

    @Override
    @Transactional
    public void saveProfileImageMeta(Long memberId, String originalFilename, String s3Key) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()) {
                });
        Profile profile = member.getProfile();

        String oldS3Key = profile.getProfileS3Key();

        if (Objects.equals(oldS3Key, s3Key)) {
            return;
        }
        if (oldS3Key != null && !oldS3Key.equals(DEFAULT_PROFILE_IMAGE_KEY)) {
            try {
                s3FileManager.delete(oldS3Key);
            } catch (RuntimeException e) {
                throw new S3DeletionFailedException("S3 삭제 실패");
            }
        }
        profile.updateProfileS3Key(s3Key);
    }

    @Transactional
    @Override
    public String getProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()) {
                });

        Profile profile = member.getProfile();
        String s3Key = profile.getProfileS3Key();
        if (s3Key == null) {
            return s3FileManager.getPresignedDownloadUrl(DEFAULT_PROFILE_IMAGE_KEY);
        }
        return s3FileManager.getPresignedDownloadUrl(s3Key);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfileInfo(CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        Member member = memberRepository.findByIdWithProfile(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
        Profile profile = member.getProfile();

        String resizedKey = S3ResizedImageKeyMapper.toResizedKey(profile.getProfileS3Key());

        ProfileResponse response = ProfileResponse.builder().memberId(member.getId())
                .profileImageUrl(getProfileImageUrl(resizedKey))
                .description(profile.getDescription()).isGalleryPublic(profile.isGalleryPublic())
                .isVideoPublic(profile.isVideoPublic())
                .isCollectionPublic(profile.isCollectionPublic())
                .followerCount(member.getFollowerCount()).followingCount(member.getFollowingCount())
                .build();

        return response;
    }

    @Transactional
    @Override
    public ProfileResponse getProfileInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()) {
                });
        Profile profile = member.getProfile();

        ProfileResponse response = ProfileResponse.builder().memberId(memberId)
                .profileImageUrl(getProfileImageUrl(profile.getProfileS3Key()))
                .description(profile.getDescription()).isGalleryPublic(profile.isGalleryPublic())
                .isVideoPublic(profile.isVideoPublic())
                .isCollectionPublic(profile.isCollectionPublic())
                .followerCount(member.getFollowerCount()).followingCount(member.getFollowingCount())
                .name(member.getName())
                .build();
        return response;
    }

    @Override
    @Transactional
    public void updateVisibility(CustomUserDetails userDetails,
            ProfileVisibilityUpdateRequest profileVisibilityUpdateRequest) {
        Long memberId = userDetails.getMember().getId();
        Member member = memberRepository.findByIdWithProfile(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));
        Profile profile = member.getProfile();
        profile.setGalleryPublic(profileVisibilityUpdateRequest.isGalleryPublic());
        profile.setCollectionPublic(profileVisibilityUpdateRequest.isCollectionPublic());
        profile.setVideoPublic(profileVisibilityUpdateRequest.isVideoPublic());

    }

    @Transactional
    public void deleteProfileImage(CustomUserDetails details) {
        if (details == null || details.getMember() == null) {
            throw new MemberNotFoundException("");
        }
        Long memberId = details.getMember().getId();

        Member member = memberRepository.findByIdWithProfile(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));

        Profile profile = member.getProfile();
        String s3Key = profile.getProfileS3Key();

        if (s3Key != null && !DEFAULT_PROFILE_IMAGE_KEY.equals(s3Key)) {
            try {
                s3FileManager.delete(s3Key);
            } catch (RuntimeException e) {
                throw new S3DeletionFailedException("S3 삭제 실패");
            }
        }
        profile.updateProfileS3Key(null);

    }

    @Override
    @Transactional
    public void updateDescription(CustomUserDetails details,
            UpdateDescriptionrequest updateDescriptionrequest) {
        Long memberId = details.getMember().getId();

        Member member = memberRepository.findByIdWithProfile(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()));

        Profile profile = member.getProfile();

        String desc = updateDescriptionrequest.getDescription();
        if (desc != null) {
            desc = desc.trim();
            if (desc.isEmpty()) {
                desc = null;
            }
            if (desc != null && desc.length() > 255) {
                throw new DescriptionTooLongException(profile.getId().toString());
            }
        }

        profile.updateDescription(desc);
    }

}
