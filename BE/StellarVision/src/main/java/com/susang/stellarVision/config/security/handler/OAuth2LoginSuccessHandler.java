package com.susang.stellarVision.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.susang.stellarVision.application.auth.jwt.dto.MemberInfoDTO;
import com.susang.stellarVision.application.auth.jwt.dto.TokenResponseDTO;
import com.susang.stellarVision.application.auth.jwt.service.RefreshTokenService;
import com.susang.stellarVision.application.member.service.MemberService;
import com.susang.stellarVision.common.utils.JWTUtil;
import com.susang.stellarVision.entity.Member;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refresh-expmin}")
    private long refreshExpmin;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String sub = (String) oAuth2User.getAttribute("sub");
        String email = (String) oAuth2User.getAttribute("email");
        String name = (String) oAuth2User.getAttribute("name");

        log.debug("[구글 로그인 정보]: {}, {}, {}",sub,email,name);

        Member member = memberService.upsertFromGoogle(sub, email, name);

        String accessToken = jwtUtil.createAccessToken(member);
        String refreshToken = jwtUtil.createRefreshToken(member);
        refreshTokenService.store(member.getEmail(), refreshToken, Duration.ofMinutes(refreshExpmin));
        memberService.updateLatestLogin(member.getId());

        TokenResponseDTO payload = new TokenResponseDTO(
                accessToken,
                refreshToken,
                new MemberInfoDTO(member.getEmail(), member.getName())
        );
        writeOk(response, payload);

    }

    private void writeOk(HttpServletResponse res, Object body) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setStatus(HttpServletResponse.SC_OK);
        objectMapper.writeValue(res.getWriter(), body);
    }
}
