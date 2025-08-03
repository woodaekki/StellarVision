package com.susang.stellarVision.common.utils;

import com.susang.stellarVision.application.auth.email.dto.EmailMessageDTO;
import com.susang.stellarVision.application.auth.email.dto.EmailType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailUtil {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendEmail(EmailMessageDTO emailMessage, EmailType type) {
        Map<String, Object> map =  new HashMap<>();
        map.put("code", emailMessage.getMessage());

        String html = setContext(map, type);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(emailMessage.getTo());
            helper.setSubject(emailMessage.getSubject());
            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            log.error("[ERROR] send-email: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public String setContext(Map<String, Object> vars, EmailType type) {
        Context context = new Context();
        vars.forEach(context::setVariable);
        return templateEngine.process(type.getTemplateName(), context);
    }

    public String createCode(int length) {
        SecureRandom random = new SecureRandom();
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
