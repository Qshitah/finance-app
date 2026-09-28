package com.marouan.finance_app.web.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// my own page shape, so the API doesn't leak Spring's internal Page JSON
public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}
