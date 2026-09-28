package com.marouan.finance_app.web;

import com.marouan.finance_app.repository.AccountEventRepository;
import com.marouan.finance_app.repository.AccountRepository;
import com.marouan.finance_app.service.AppendEventCommand;
import com.marouan.finance_app.service.EventService;
import com.marouan.finance_app.web.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts/{accountId}")
@RequiredArgsConstructor
public class AccountController {

    private final EventService eventService;
    private final AccountRepository accountRepository;
    private final AccountEventRepository eventRepository;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse append(@PathVariable UUID accountId, @Valid @RequestBody AppendEventRequest req) {
        var cmd = new AppendEventCommand(accountId, req.eventType(), req.amount(), req.currency(),
                req.categoryId(), req.description(), req.transferGroupId(), req.payload(), req.occurredAt());
        return EventResponse.from(eventService.append(cmd));
    }

    // newest first, now returns the page info too
    @GetMapping("/events")
    public PageResponse<EventResponse> list(@PathVariable UUID accountId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        var result = eventRepository.findByAccountIdOrderBySequenceNoDesc(
                accountId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return PageResponse.from(result.map(EventResponse::from));
    }

    // cached balance (V2), instant
    @GetMapping("/balance")
    public BalanceResponse balance(@PathVariable UUID accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
        return new BalanceResponse(account.getId(), account.getBalance(), account.getBaseCurrency());
    }

    // replays every event and compares with the cached balance
    @GetMapping("/balance/check")
    public ConsistencyResponse check(@PathVariable UUID accountId) {
        return new ConsistencyResponse(accountId, eventService.isBalanceConsistent(accountId));
    }
}