package com.susang.stellarVision.application.collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CollectionResponse {
    private Integer id;
    private String name;         // 영문 이름 (예: Aries)
    private String koreanName;   // 한글 이름 (예: 양자리)
    private String abbreviation; // 약어 (예: Ari)
    private boolean collected;   // 로그인 유저가 수집했는지 여부
}
