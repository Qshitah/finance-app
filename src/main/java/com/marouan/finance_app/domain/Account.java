package com.marouan.finance_app.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
// JPA needs an empty constructor, but I don't want anyone using it by accident
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // just the id for now, the User entity comes with Spring Security
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    // the V2 cached balance, only changed through applyDelta below
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Account(UUID userId, String name, AccountType type, String baseCurrency) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.baseCurrency = baseCurrency;
    }

    // no setBalance on purpose: the balance can only move by adding an event's amount
    public void applyDelta(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}