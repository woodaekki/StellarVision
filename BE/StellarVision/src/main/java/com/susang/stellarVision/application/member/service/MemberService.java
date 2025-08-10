package com.susang.stellarVision.application.member.service;

import com.susang.stellarVision.application.member.dto.MemberSearchListDTO;
import com.susang.stellarVision.application.member.dto.SignUpRequest;
import com.susang.stellarVision.entity.Member;

public interface MemberService {

    Member getMemberByEmail(String email);

    Long registerMember(SignUpRequest request);

    void updateLatestLogin(Long memberId);

    MemberSearchListDTO searchMembers(String name, Long cursor, int limit);

    Member upsertFromGoogle(String googleSub, String email, String name);
}
