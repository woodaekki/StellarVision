package com.susang.stellarVision.application.auth.email.service;

import com.susang.stellarVision.application.auth.email.dto.SendCodeRequest;

public interface EmailAuthService {
    void sendVerificationCode(SendCodeRequest sendCodeRequest);
}
