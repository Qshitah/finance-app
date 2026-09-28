package com.marouan.finance_app.web.dto;

import java.util.UUID;

public record ConsistencyResponse(UUID accountId, boolean consistent) {}
