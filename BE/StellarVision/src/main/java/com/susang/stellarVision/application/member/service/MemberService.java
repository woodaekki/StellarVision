package com.susang.stellarVision.application.member.service;

import com.susang.stellarVision.entity.Member;

public interface MemberService {
    Member getMemberByEmail(String email);

    boolean existsByEmail(String email);
}
