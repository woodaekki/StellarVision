package com.susang.stellarVision.config.security.authentication;

import com.susang.stellarVision.application.member.service.MemberService;
import com.susang.stellarVision.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("user-email: {}", username);
        Member member = memberService.getMemberByEmail(username);
        log.debug("member: {}", member);

        if(member == null) {
            throw new UsernameNotFoundException("[ERROR] 해당 이메일의 사용자를 찾지 못했습니다. " + username);
        }

        return new CustomUserDetails(member);
    }

}