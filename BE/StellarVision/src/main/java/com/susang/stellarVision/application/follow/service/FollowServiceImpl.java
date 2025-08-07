package com.susang.stellarVision.application.follow.service;

import com.susang.stellarVision.application.follow.exception.AlreadyFollowException;
import com.susang.stellarVision.application.follow.exception.FollowNotFoundException;
import com.susang.stellarVision.application.follow.repository.FollowRepository;
import com.susang.stellarVision.application.follow.dto.FollowMemberDTO;
import com.susang.stellarVision.application.member.exception.MemberNotFoundException;
import com.susang.stellarVision.application.member.repository.MemberRepository;
import com.susang.stellarVision.application.profile.service.ProfileService;
import com.susang.stellarVision.common.exception.AccessDeniedException;
import com.susang.stellarVision.entity.Follow;
import com.susang.stellarVision.entity.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final MemberRepository memberRepository;
    private final ProfileService profileService;
    private final FollowRepository followRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FollowMemberDTO> getFollowers(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
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
    @Transactional(readOnly = true)
    public List<FollowMemberDTO> getFollowings(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(memberId.toString());
        }

        return followRepository.findFollowingMembersByFromMemberId(memberId)
                .stream()
                .peek(dto ->
                        dto.setProfileImageUrl(
                                profileService.getProfileImageUrl(dto.getProfileImageUrl())))
                .toList();
    }

    @Override
    @Transactional
    public Long followMember(Member fromMember, Long toMemberId) {
        Member toMember = memberRepository.findById(toMemberId)
                .orElseThrow(() -> new MemberNotFoundException(toMemberId.toString()));

        if (followRepository.existsByFromMember_IdAndToMember_Id(fromMember.getId(),
                toMember.getId())) {
            throw new AlreadyFollowException(toMember.getId()
                    .toString());
        }

        Follow follow = Follow.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .build();

        fromMember.increaseFollowingCount();
        toMember.increaseFollowerCount();

        followRepository.save(follow);

        return follow.getId();
    }

    @Override
    @Transactional
    public void deleteFollow(Long followId, Long memberId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new FollowNotFoundException(followId.toString()));

        Member fromMember = follow.getFromMember();
        Member toMember = follow.getToMember();

        if (fromMember.getId().equals(memberId)) {
            fromMember.decreaseFollowingCount();
        } else if (toMember.getId().equals(memberId)) {
            toMember.decreaseFollowerCount();
        } else {
            throw new AccessDeniedException("삭제 권한이 없습니다");
        }

        followRepository.deleteById(followId);
    }


}
