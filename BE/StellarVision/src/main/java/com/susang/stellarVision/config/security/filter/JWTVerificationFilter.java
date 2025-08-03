package com.susang.stellarVision.config.security.filter;

import com.susang.stellarVision.config.security.authentication.CustomUserDetails;
import com.susang.stellarVision.config.security.authentication.CustomUserDetailsService;
import com.susang.stellarVision.common.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTVerificationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token == null) {
            log.debug("[JWTVerificationFilter] 요청전 실행됨");
            filterChain.doFilter(request, response);
            log.debug("[JWTVerificationFilter] 응답 실행됨");
            return;
        }

        Claims claims = jwtUtil.getClaims(token);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService
                .loadUserByUsername(claims.get("email")
                        .toString());

        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        log.debug("[JWTVerificationFilter] 요청 실행됨");
        filterChain.doFilter(request, response);
        log.debug("[JWTVerificationFilter] 응답 실행됨");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
