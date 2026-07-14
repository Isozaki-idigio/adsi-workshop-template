package com.example.attendance.common.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business error: code={}, message={}", ex.getCode(), ex.getMessage());
        var response = new ErrorResponse(ex.getMessage(), ex.getCode());
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse.FieldErrorDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetail)
                .toList();
        var response = new ValidationErrorResponse("バリデーションエラー", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        var response = new ErrorResponse("内部エラーが発生しました", "INTERNAL_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ValidationErrorResponse.FieldErrorDetail toDetail(FieldError fieldError) {
        return new ValidationErrorResponse.FieldErrorDetail(
                fieldError.getField(),
                fieldError.getDefaultMessage());
    }

    public record ErrorResponse(String message, String code) {}

    public record ValidationErrorResponse(
            String message,
            List<FieldErrorDetail> errors) {
        public record FieldErrorDetail(String field, String message) {}
    }
}
