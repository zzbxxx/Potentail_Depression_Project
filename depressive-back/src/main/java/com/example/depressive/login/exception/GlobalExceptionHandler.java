package com.example.depressive.login.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCode(InvalidCodeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_CODE", ex.getMessage()));
    }

    @ExceptionHandler(ExpiredCodeException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCode(ExpiredCodeException ex) {
        return ResponseEntity.status(HttpStatus.GONE)
                .body(new ErrorResponse("EXPIRED_CODE", ex.getMessage()));
    }

    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}
