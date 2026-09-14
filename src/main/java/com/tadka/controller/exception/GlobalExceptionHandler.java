package com.tadka.controller.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ConstraintViolationException ex) {
        return build(400, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
            errors.put(fe.getField(), fe.getDefaultMessage()));
        return build(400, errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        return build(400, ex.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return build(404, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleDomainViolation(IllegalStateException ex) {
        return build(422, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrency(OptimisticLockingFailureException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "CONCURRENT_MODIFICATION");
        body.put("message", "This resource was modified by another request. Please refresh and retry.");
        body.put("status", 409);
        return ResponseEntity.status(409).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return build(409, "A duplicate request was detected. If you already placed an order, use the same Idempotency-Key.");
    }

    private ResponseEntity<Map<String, Object>> build(int status, Object detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        body.put("detail", detail);
        body.put("status", status);
        body.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.status(status).body(body);
    }
}
