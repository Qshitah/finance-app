package com.marouan.finance_app.service;

import com.marouan.finance_app.domain.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

// everything needed to append one event, the service fills in the rest (sequence, recorded_at)
public record AppendEventCommand(
        UUID accountId,
        EventType eventType,
        BigDecimal amount,
        String currency,
        UUID categoryId,
        String description,
        UUID transferGroupId,
        Map<String, Object> payload,
        OffsetDateTime occurredAt
) {}