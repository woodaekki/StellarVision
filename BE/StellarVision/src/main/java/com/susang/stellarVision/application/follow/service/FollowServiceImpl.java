package com.susang.stellarVision.application.follow.service;

import com.susang.stellarVision.application.follow.repository.FollowRepository;
import com.susang.stellarVision.application.follow.dto.FollowMemberDTO;
import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.profile.service.ProfileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final MemberRepository memberRepository;
    private final ProfileService profileService;
    private final FollowRepository followRepository;

    @Override
    public List<FollowMemberDTO> getFollowers(Long memberId) {
        if(!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(memberId.toString());
        }

        return followRepository.findFollowerMembersByToMemberId(memberId)
                .stream()
                .peek(dto ->
                        dto.setProfileImageUrl(
                                profileService.getProfileImageUrl(dto.getProfileImageUrl())))
                .toList();
    }

    @Override
    public List<FollowMemberDTO> getFollowings(Long memberId) {
        if(!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(memberId.toString());
        }

        return followRepository.findFollowingMembersByFromMemberId(memberId)
                .stream()
                .peek(dto ->
                        dto.setProfileImageUrl(
                                profileService.getProfileImageUrl(dto.getProfileImageUrl())))
                .toList();
    }
}
