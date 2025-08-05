package com.susang.stellarVision.application.member.controller;

import com.susang.stellarVision.application.member.dto.MemberAccountInfoDTO;
import com.susang.stellarVision.application.member.dto.SignUpRequest;
import com.susang.stellarVision.application.member.service.MemberService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.entity.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<APIResponse<MemberAccountInfoDTO>> getMember(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        Member member = customUserDetails.getMember();
        MemberAccountInfoDTO data = new MemberAccountInfoDTO(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getBirth(),
                member.getFollowerCount(),
                member.getFollowingCount()
        );

        return ResponseEntity.ok(APIResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<APIResponse<Long>> signup(@Valid @RequestBody SignUpRequest request) {
        Long memberId = memberService.registerMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success(memberId));
    }
}
