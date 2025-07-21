package com.example.recycling_app.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body("입력 값이 잘못되었습니다.");
    }

    // 비즈니스 로직 상의 불가 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalException(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}

