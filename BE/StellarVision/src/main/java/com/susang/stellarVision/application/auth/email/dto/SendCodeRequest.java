package com.susang.stellarVision.application.auth.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendCodeRequest {
    @Email(message = "유효한 이메일 주소여야 합니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
}
