package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.domain.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID id, String name, String type, BigDecimal balance, String baseCurrency) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getName(), a.getType().name(), a.getBalance(), a.getBaseCurrency());
    }
}