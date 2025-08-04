package com.susang.stellarVision.application.auth.email.service;

import com.susang.stellarVision.application.auth.email.dto.SendCodeRequest;
import com.susang.stellarVision.application.auth.email.dto.VerifyCodeRequest;

public interface EmailAuthService {

    void sendVerificationCode(SendCodeRequest sendCodeRequest);

    void verifyCode(VerifyCodeRequest verifyCodeRequest);
}
