package com.marouan.finance_app.web.dto;

import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.util.List;

// the only shape an error can have, so the frontend needs just one type for failures
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String code,          // stable machine-readable value the frontend can switch on
        String message,       // human-readable, fine to show in a toast
        String path,
        List<FieldIssue> fieldErrors   // always an array, empty when not a validation error
) {
    public record FieldIssue(String field, String message) {}

    public static ErrorResponse of(HttpStatus status, String code, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status.value(), code, message, path, List.of());
    }

    public static ErrorResponse validation(String path, List<FieldIssue> issues) {
        return new ErrorResponse(OffsetDateTime.now(), HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED", "Request validation failed", path, issues);
    }
}