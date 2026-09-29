package com.marouan.finance_app.web.dto;

public record LoginResponse(String token, String tokenType) {
    public static LoginResponse of(String token) {
        return new LoginResponse(token, "Bearer");
    }
}