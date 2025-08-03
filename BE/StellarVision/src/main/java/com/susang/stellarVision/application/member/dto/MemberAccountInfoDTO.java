package com.susang.stellarVision.application.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberAccountInfoDTO {
    private Long id;
    private String email;
    private String name;
    private LocalDate birth;
    private Long followerCount;
    private Long followingCount;
}
