package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.domain.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(@NotBlank @Size(max = 100) String name, @NotNull CategoryType type) {}