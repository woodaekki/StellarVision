package com.susang.stellarVision.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class APIError {
    private String code;      // 예: "USER_001", "COMMON_500" 등
    private String details;   // 상세 설명 (null 가능)
}
