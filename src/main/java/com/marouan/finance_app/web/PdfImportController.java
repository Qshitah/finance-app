package com.marouan.finance_app.web;

import com.marouan.finance_app.security.AppUserDetails;
import com.marouan.finance_app.service.AppendEventCommand;
import com.marouan.finance_app.service.EventService;
import com.marouan.finance_app.service.PdfImportService;
import com.marouan.finance_app.web.dto.ImportConfirmRequest;
import com.marouan.finance_app.web.dto.ImportPreviewResponse;
import com.marouan.finance_app.web.dto.ImportResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts/{accountId}/import")
@RequiredArgsConstructor
public class PdfImportController {

    private final PdfImportService pdfImportService;
    private final EventService eventService;

    // step 1: upload, get back proposed lines, nothing saved yet
    @PostMapping("/preview")
    public ImportPreviewResponse preview(@PathVariable UUID accountId,
                                         @RequestParam("file") MultipartFile file) {
        return ImportPreviewResponse.of(pdfImportService.extract(file));
    }

    // step 2: the reviewed/edited list actually gets appended as real events
    @PostMapping("/confirm")
    public ImportResultResponse confirm(@PathVariable UUID accountId,
                                        @Valid @RequestBody ImportConfirmRequest req,
                                        @AuthenticationPrincipal AppUserDetails caller) {
        int count = 0;
        for (var line : req.events()) {
            var cmd = new AppendEventCommand(
                    accountId, line.eventType(), line.amount(), "EUR", // currency: account's own for now
                    line.categoryId(), line.description(), null, null,
                    line.date().atStartOfDay(java.time.ZoneOffset.UTC).toOffsetDateTime());
            eventService.append(cmd, caller.getId());
            count++;
        }
        return new ImportResultResponse(count);
    }
}