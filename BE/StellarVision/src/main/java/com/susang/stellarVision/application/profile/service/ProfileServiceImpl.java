package com.susang.stellarVision.application.profile.service;

import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.photo.error.S3DeletionFailedException;
import com.susang.stellarVision.application.profile.dto.ProfileResponse;
import com.susang.stellarVision.common.s3.S3FileManager;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.entity.Member;
import com.susang.stellarVision.entity.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final MemberRepository memberRepository;
    private final S3FileManager s3FileManager;

    @Override
    @Transactional
    public void saveProfileImageMeta(Long memberId, String originalFilename, String s3Key) throws S3DeletionFailedException, MemberNotFoundException  {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()) {
                });
        Profile profile = member.getProfile();

        String oldS3Key = profile.getProfileS3Key();

        // 중복 요청 무시
        if (s3Key.equals(oldS3Key)) {
            return;
        }
        if (oldS3Key != null) {
            try {
                s3FileManager.delete(s3Key);
            } catch (Exception e) {
                 throw new S3DeletionFailedException("S3 삭제 실패");
            }
        }
        profile.setProfileS3Key(s3Key);
    }

    @Override
    public String getProfileImage(Long memberId) throws MemberNotFoundException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()) {
                });

        Profile profile = member.getProfile();
        String s3Key = profile.getProfileS3Key();

        if (s3Key == null) {
            // Todo : 기본이미지
        }
        return s3FileManager.getPresignedDownloadUrl(s3Key);
    }

    @Override
    public ProfileResponse getMyProfileInfo(CustomUserDetails userDetails) throws MemberNotFoundException {
        Member member = userDetails.getMember();
        if (member == null) {
            throw new MemberNotFoundException("존재하지 않는 멤버입니다.");
        }
        Profile profile = member.getProfile();

        ProfileResponse response = ProfileResponse.builder()
                .memberId(member.getId())
                .profileImageUrl(s3FileManager.getPresignedDownloadUrl(profile.getProfileS3Key()))
                .description(profile.getDescription())
                .isGalleryPublic(profile.isGalleryPublic())
                .isVideoPublic(profile.isVideoPublic())
                .isCollectionPublic(profile.isCollectionPublic())
                .build();

        return response;
    }

    @Override
    public ProfileResponse getProfileInfo(Long memberId) throws MemberNotFoundException, MemberNotFoundException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId.toString()) {
                });
        Profile profile = member.getProfile();

        ProfileResponse response = ProfileResponse.builder()
                .memberId(memberId)
                .profileImageUrl(s3FileManager.getPresignedDownloadUrl(profile.getProfileS3Key()))
                .description(profile.getDescription())
                .isGalleryPublic(profile.isGalleryPublic())
                .isVideoPublic(profile.isVideoPublic())
                .isCollectionPublic(profile.isCollectionPublic())
                .build();
        return response;
    }


}
