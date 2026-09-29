package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.domain.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id, String username, String email, boolean enabled, String role, OffsetDateTime createdAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.isEnabled(),
                u.getRole().name(), u.getCreatedAt());
    }
}