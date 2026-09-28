package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.domain.AccountEvent;
import com.marouan.finance_app.domain.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// I return this instead of the entity so the API doesn't depend on the table layout
public record EventResponse(
        UUID id,
        UUID accountId,
        long sequenceNo,
        EventType eventType,
        BigDecimal amount,
        String currency,
        UUID categoryId,
        String description,
        OffsetDateTime occurredAt
) {
    public static EventResponse from(AccountEvent e) {
        return new EventResponse(e.getId(), e.getAccountId(), e.getSequenceNo(), e.getEventType(),
                e.getAmount(), e.getCurrency(), e.getCategoryId(), e.getDescription(), e.getOccurredAt());
    }
}