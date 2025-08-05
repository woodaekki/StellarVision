package com.susang.stellarVision.application.member.service;

import com.susang.stellarVision.application.member.dto.SignUpRequest;
import com.susang.stellarVision.entity.Member;
import java.time.LocalDateTime;

public interface MemberService {
    Member getMemberByEmail(String email);

    boolean existsByEmail(String email);

    Long registerMember(SignUpRequest request);

    void updateLatestLogin(Long memberId);
}
