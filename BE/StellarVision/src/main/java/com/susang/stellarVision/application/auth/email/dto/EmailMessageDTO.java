package com.susang.stellarVision.application.auth.email.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessageDTO {
    @Email
    private String to;
    private String subject;
    private String message;
}
