package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.domain.Category;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String type) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getType().name());
    }
}