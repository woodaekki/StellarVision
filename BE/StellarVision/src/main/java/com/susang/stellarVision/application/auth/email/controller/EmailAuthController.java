package com.susang.stellarVision.application.auth.email.controller;

import com.susang.stellarVision.application.auth.email.dto.SendCodeRequest;
import com.susang.stellarVision.application.auth.email.service.EmailAuthService;
import com.susang.stellarVision.common.dto.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<APIResponse<Void>> sendVerificationCode(@Valid @RequestBody SendCodeRequest sendCodeRequest) {
        emailAuthService.sendVerificationCode(sendCodeRequest);
        return ResponseEntity.ok(APIResponse.success(null));
    }
}
