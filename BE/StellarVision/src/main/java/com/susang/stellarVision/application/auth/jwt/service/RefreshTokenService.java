package com.susang.stellarVision.application.auth.jwt.service;

import java.time.Duration;

import com.susang.stellarVision.application.auth.jwt.error.RefreshTokenError;
import com.susang.stellarVision.application.auth.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void store(String email, String refreshToken, Duration ttl) {
        refreshTokenRepository.save(email, refreshToken, ttl);
    }

    public String get(String email) {
        return refreshTokenRepository.findByEmail(email)
                .orElseThrow(RefreshTokenError.NotFound::new);
    }

    public void delete(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }
}
