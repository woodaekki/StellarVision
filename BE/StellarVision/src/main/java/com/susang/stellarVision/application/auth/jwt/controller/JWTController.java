package com.susang.stellarVision.application.auth.jwt.controller;

import com.susang.stellarVision.application.member.service.MemberService;
import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.application.auth.jwt.dto.MemberInfoDTO;
import com.susang.stellarVision.application.auth.jwt.dto.TokenResponseDTO;
import com.susang.stellarVision.application.auth.jwt.error.RefreshTokenError;
import com.susang.stellarVision.application.auth.jwt.service.RefreshTokenService;
import com.susang.stellarVision.common.util.JWTUtil;
import com.susang.stellarVision.entity.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class JWTController {

    private final JWTUtil jwtUtil;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh-expmin}")
    private Long refreshExpMin;

    @PostMapping("/refresh")
    public ResponseEntity<APIResponse<TokenResponseDTO>> refreshAccessToken(
            @RequestHeader("Refresh-Token") String refreshToken) {

        String email = resolveRefreshEmail(refreshToken);

        // refresh 토큰 검증
        String findRefreshToken = refreshTokenService.get(email);
        if (!refreshToken.equals(findRefreshToken)) {
            refreshTokenService.delete(email);
            throw new RefreshTokenError.Mismatch();
        }

        Member member = memberService.getMemberByEmail(email);
        String accessToken = jwtUtil.createAccessToken(member);
        String newRefreshToken = jwtUtil.createRefreshToken(member);

        refreshTokenService.store(email, newRefreshToken, Duration.ofMinutes(refreshExpMin));

        MemberInfoDTO memberInfoDTO = new MemberInfoDTO(member.getEmail(), member.getName());
        TokenResponseDTO tokenResponseDTO = new TokenResponseDTO(accessToken, newRefreshToken, memberInfoDTO);

        return ResponseEntity.ok(APIResponse.success("토큰 재발급에 성공했습니다.", tokenResponseDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(@RequestHeader("Refresh-Token") String refreshToken) {

        String email = resolveRefreshEmail(refreshToken);

        refreshTokenService.delete(email);

        return ResponseEntity.ok(APIResponse.success("로그아웃되었습니다.", null));
    }

    private String resolveRefreshEmail(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new RefreshTokenError.Missing();
        }

        Claims claims;
        try {
            claims = jwtUtil.getClaims(refreshToken);
        } catch (JwtException e) {
            log.warn("Refresh token parsing failed", e);
            throw new RefreshTokenError.Invalid();
        }

        String email = (String) claims.get("email");

        if (email == null) {
            throw new RefreshTokenError.ClaimMissing();
        }

        return email;
    }
}
