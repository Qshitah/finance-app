package com.marouan.finance_app.web.dto;

import com.marouan.finance_app.service.dto.ProposedEvent;

import java.util.List;

public record ImportPreviewResponse(List<ProposedEvent> proposed, int totalFound) {
    public static ImportPreviewResponse of(List<ProposedEvent> proposed) {
        return new ImportPreviewResponse(proposed, proposed.size());
    }
}