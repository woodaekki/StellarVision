package com.susang.stellarVision.application.follow.service;

import com.susang.stellarVision.application.follow.dto.FollowMemberDTO;
import com.susang.stellarVision.entity.Member;
import java.util.List;

public interface FollowService {

    List<FollowMemberDTO> getFollowers(Long memberId);

    List<FollowMemberDTO> getFollowings(Long memberId);

    Long followMember(Member fromMember, Long toMemberId);

    void deleteFollow(Long followId, Long memberId);
}
