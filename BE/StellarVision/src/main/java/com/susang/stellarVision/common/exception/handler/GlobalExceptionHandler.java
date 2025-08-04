package com.susang.stellarVision.common.exception.handler;

import com.susang.stellarVision.common.dto.APIResponse;
import com.susang.stellarVision.common.exception.AlreadyExistException;
import com.susang.stellarVision.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<APIResponse<Void>> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(APIResponse.fail("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<APIResponse<Void>> handleAlreadyExistException(AlreadyExistException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(APIResponse.fail("ALREADY_EXIST", e.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(APIResponse.fail("INTERNAL_SERVER_ERROR", "알 수 없는 서버 오류가 발생했습니다.",
                        e.getMessage()));
    }
}
