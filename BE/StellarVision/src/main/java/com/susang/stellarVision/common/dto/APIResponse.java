package com.susang.stellarVision.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class APIResponse<T> {
    private String status;   // "success" or "error"
    private T data;
    private String message;
    private APIError error;

    // 성공 응답: 데이터만
    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>("success", data, "요청에 성공했습니다.", null);
    }

    // 성공 응답: 메시지 + 데이터
    public static <T> APIResponse<T> success(String message, T data) {
        return new APIResponse<>("success", data, message, null);
    }

    // 실패 응답: 에러 코드와 메시지
    public static <T> APIResponse<T> fail(String code, String message) {
        return new APIResponse<>("error", null, message, new APIError(code, null));
    }

    // 실패 응답: 에러 코드 + 메시지 + 상세 설명
    public static <T> APIResponse<T> fail(String code, String message, String details) {
        return new APIResponse<>("error", null, message, new APIError(code, details));
    }
}
