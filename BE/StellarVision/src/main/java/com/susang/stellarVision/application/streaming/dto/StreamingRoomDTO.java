package com.susang.stellarVision.application.streaming.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StreamingRoomDTO {
    private Long id;
    private String sessionId;
    private String title;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long ownerMemberId;
    private String ownerMemberName;
}
