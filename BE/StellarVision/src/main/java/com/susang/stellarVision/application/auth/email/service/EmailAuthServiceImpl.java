package com.susang.stellarVision.application.auth.email.service;

import com.susang.stellarVision.application.auth.email.dto.EmailMessageDTO;
import com.susang.stellarVision.application.auth.email.dto.EmailType;
import com.susang.stellarVision.application.auth.email.dto.SendCodeRequest;
import com.susang.stellarVision.application.auth.email.repository.EmailAuthTokenRepository;
import com.susang.stellarVision.application.member.exception.MemberAlreadyExistException;
import com.susang.stellarVision.application.member.service.MemberService;
import com.susang.stellarVision.common.utils.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailAuthServiceImpl implements EmailAuthService {

    private final MemberService memberService;
    private final EmailAuthTokenRepository emailAuthTokenRepository;
    private final EmailUtil emailUtil;

    @Override
    public void sendVerificationCode(SendCodeRequest sendCodeRequest) {

        String email = sendCodeRequest.getEmail();
        if(memberService.existsByEmail(email)) {
            throw new MemberAlreadyExistException(email);
        }

        String code = emailUtil.createCode(6);
        log.debug("Sending verification code to email: {}", code);

        emailAuthTokenRepository.saveCode(email, code, Duration.ofMinutes(5));

        EmailMessageDTO message = new EmailMessageDTO(email, "[StellarVision] 회원가입 이메일 인증 코드", code);

        emailUtil.sendEmail(message, EmailType.SIGNUP);
    }
}
