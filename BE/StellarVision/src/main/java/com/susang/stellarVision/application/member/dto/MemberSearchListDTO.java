package com.susang.stellarVision.application.member.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchListDTO {
    private List<MemberSearchInfoDTO> members;
    private PageInfo pageInfo;


    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageInfo {
        private Long nextCursor;
        private boolean hasNext;
        private int limit;
    }
}
