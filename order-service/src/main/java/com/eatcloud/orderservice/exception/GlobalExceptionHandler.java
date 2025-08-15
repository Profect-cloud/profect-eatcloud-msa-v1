package com.eatcloud.orderservice.exception;

import com.eatcloud.orderservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CartException.class)
    public ResponseEntity<ApiResponse<Object>> handleCartException(CartException e) {
        log.error("Cart exception occurred: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument exception: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception occurred: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unexpected exception occurred: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 오류가 발생했습니다."));
    }
}
