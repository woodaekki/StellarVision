package com.susang.stellarVision.application.auth.email.controller;

import com.susang.stellarVision.application.auth.email.dto.SendCodeRequest;
import com.susang.stellarVision.application.auth.email.dto.VerifyCodeRequest;
import com.susang.stellarVision.application.auth.email.exception.VerificationCodeException;
import com.susang.stellarVision.application.auth.email.service.EmailAuthService;
import com.susang.stellarVision.common.dto.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    @PostMapping("/send")
    public ResponseEntity<APIResponse<Void>> sendVerificationCode(
            @Valid @RequestBody SendCodeRequest sendCodeRequest) {
        emailAuthService.sendVerificationCode(sendCodeRequest);
        return ResponseEntity.ok(APIResponse.success(null));
    }

    @PostMapping("/verification")
    public ResponseEntity<APIResponse<Void>> verificationCode(
            @Valid @RequestBody VerifyCodeRequest verifyCodeRequest) {
        try {
            emailAuthService.verifyCode(verifyCodeRequest);
            return ResponseEntity.ok(APIResponse.success("인증에 성공했습니다", null));
        } catch (VerificationCodeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(APIResponse.fail("Email", "인증에 실패했습니다", e.getMessage()));
        }
    }
}
