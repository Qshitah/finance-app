package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull AccountType type,
        @NotBlank @Size(min = 3, max = 3) String baseCurrency
) {}
