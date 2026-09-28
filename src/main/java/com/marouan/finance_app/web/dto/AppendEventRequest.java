package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

// what the client sends, the account id comes from the URL so it's not here
public record AppendEventRequest(
        @NotNull EventType eventType,
        @NotNull BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        UUID categoryId,
        @Size(max = 255) String description,
        UUID transferGroupId,
        Map<String, Object> payload,
        @NotNull OffsetDateTime occurredAt
) {}