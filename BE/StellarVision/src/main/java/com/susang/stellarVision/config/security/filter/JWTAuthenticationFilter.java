package com.susang.stellarVision.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.susang.stellarVision.application.member.service.MemberService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.application.auth.jwt.dto.MemberInfoDTO;
import com.susang.stellarVision.application.auth.jwt.dto.TokenResponseDTO;
import com.susang.stellarVision.application.auth.jwt.service.RefreshTokenService;
import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.common.utils.JWTUtil;
import com.susang.stellarVision.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final RefreshTokenService refreshTokenService;
    private final JWTUtil jwtUtil;
    private final long refreshExpMin;
    private final MemberService memberService;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager,
            RefreshTokenService refreshTokenService, JWTUtil jwtUtil,
            MemberService memberService,
            @Value("${jwt.refresh-expmin}") long refreshExpMin) {
        super(authenticationManager);
        this.refreshExpMin = refreshExpMin;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.memberService = memberService;
        this.setFilterProcessesUrl("/api/auth/login");
        this.setUsernameParameter("email");
        this.setPasswordParameter("password");
    }

    // 로그인 성공 시 실행하는 메소드 (JWT 발급)
    @Override
    public void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain,
            Authentication authentication) {
        log.debug("[JWTAuthenticationFilter] 실행됨");

        String rememberMe = request.getParameter("remember-me");

        Cookie cookie;
        if (Objects.isNull(rememberMe)) {
            cookie = new Cookie("remember-me", null);
            cookie.setMaxAge(0);
        } else {
            cookie = new Cookie("remember-me", rememberMe);
            cookie.setMaxAge(60 * 60 * 30);
        }

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.createAccessToken(details.getMember());
        String refreshToken = jwtUtil.createRefreshToken(details.getMember());

        Member member = details.getMember();
        refreshTokenService.store(member.getEmail(), refreshToken,
                Duration.ofMinutes(refreshExpMin));

        memberService.updateLatestLogin(member.getId());

        MemberInfoDTO memberInfoDTO = new MemberInfoDTO(member.getEmail(), member.getName());
        TokenResponseDTO dto = new TokenResponseDTO(accessToken, refreshToken, memberInfoDTO);
        APIResponse<TokenResponseDTO> apiResponse = APIResponse.success("로그인에 성공했습니다.", dto);

        try {
            writeAPIResponse(response, apiResponse, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 로그인 실패 시 실행하는 메소드
    @Override
    public void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) {
        log.debug("[JWTAuthenticationFilter] 실행됨");
        throw failed;
    }

    // 결과 전송 helper 메소드
    private void writeAPIResponse(HttpServletResponse response, APIResponse<?> apiResponse,
            HttpStatus status) throws IOException {
        log.debug("[JWTAuthenticationFilter] 실행됨");
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value());
        String json = new ObjectMapper().writeValueAsString(apiResponse);
        response.getWriter()
                .write(json);
    }
}
