package com.marouan.finance_app.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "account_events")
// tells Hibernate to never even try an UPDATE on this table
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    // position in this account's history, the service decides it, not me
    @Column(name = "sequence_no", nullable = false, updatable = false)
    private long sequenceNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false, length = 20)
    private EventType eventType;

    @Column(nullable = false, updatable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, length = 3)
    private String currency;

    @Column(name = "category_id", updatable = false)
    private UUID categoryId;

    @Column(updatable = false)
    private String description;

    @Column(name = "transfer_group_id", updatable = false)
    private UUID transferGroupId;

    // free-form extras (source PDF, import id...), stored as jsonb
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(updatable = false)
    private Map<String, Object> payload;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private OffsetDateTime recordedAt = OffsetDateTime.now();

    public AccountEvent(UUID accountId, long sequenceNo, EventType eventType, BigDecimal amount,
                        String currency, UUID categoryId, String description,
                        UUID transferGroupId, Map<String, Object> payload, OffsetDateTime occurredAt) {

        // catch obvious mistakes here instead of storing them forever
        if (eventType == EventType.CREDIT && amount.signum() < 0) {
            throw new IllegalArgumentException("CREDIT can't have a negative amount");
        }
        if (eventType == EventType.DEBIT && amount.signum() > 0) {
            throw new IllegalArgumentException("DEBIT can't have a positive amount");
        }

        this.accountId = accountId;
        this.sequenceNo = sequenceNo;
        this.eventType = eventType;
        this.amount = amount;
        this.currency = currency;
        this.categoryId = categoryId;
        this.description = description;
        this.transferGroupId = transferGroupId;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }
}
