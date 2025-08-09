package com.susang.stellarVision.application.member.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchInfoDTO {
    private Long memberId;
    private String name;
    private String profileImageUrl;
    private LocalDateTime createdAt;
}
