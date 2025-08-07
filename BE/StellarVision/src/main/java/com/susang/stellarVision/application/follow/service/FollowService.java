package com.susang.stellarVision.application.follow.service;

import com.susang.stellarVision.application.follow.dto.FollowMemberDTO;
import java.util.List;

public interface FollowService {
    List<FollowMemberDTO> getFollowers(Long memberId);
    List<FollowMemberDTO> getFollowings(Long memberId);
}
