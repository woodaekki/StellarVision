package com.susang.stellarVision.application.follow.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FollowMemberDTO {
    private Long memberId;
    private Long followerId;
    private String name;
    private String profileImageUrl;
    private LocalDateTime createdAt;
}
