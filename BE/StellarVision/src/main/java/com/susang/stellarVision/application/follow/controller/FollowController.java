package com.susang.stellarVision.application.follow.controller;

import com.susang.stellarVision.application.follow.dto.FollowRequest;
import com.susang.stellarVision.application.follow.service.FollowService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public ResponseEntity<APIResponse<Long>> followToMember(
            @RequestBody FollowRequest followRequest,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Long followId = followService.followMember(customUserDetails.getMember(), followRequest.getToMemberId());
        return ResponseEntity.ok(APIResponse.success(followId));
    }

    @DeleteMapping("/{followId}")
    public ResponseEntity<APIResponse<Long>> unfollowToMember(
            @PathVariable Long followId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        Long memberId = customUserDetails.getMember().getId();
        followService.deleteFollow(followId, memberId);
        return ResponseEntity.ok(APIResponse.success("팔로우를 취소했습니다.", null));
    }
}
