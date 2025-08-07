package com.susang.stellarVision.application.follow.repository;

import com.susang.stellarVision.application.follow.dto.FollowMemberDTO;
import java.util.List;

public interface FollowRepositoryCustom {

    List<FollowMemberDTO> findFollowingMembersByFromMemberId(Long fromMemberId);

    List<FollowMemberDTO> findFollowerMembersByToMemberId(Long toMemberId);
}
