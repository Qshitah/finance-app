package com.marouan.finance_app.web;

import com.marouan.finance_app.repository.AccountEventRepository;
import com.marouan.finance_app.security.AppUserDetails;
import com.marouan.finance_app.service.AccountService;
import com.marouan.finance_app.service.AppendEventCommand;
import com.marouan.finance_app.service.EventService;
import com.marouan.finance_app.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts/{accountId}")
@RequiredArgsConstructor
public class AccountController {

    private final EventService eventService;
    private final AccountService accountService;
    private final AccountEventRepository eventRepository;

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse append(@PathVariable UUID accountId, @Valid @RequestBody AppendEventRequest req,
                                @AuthenticationPrincipal AppUserDetails caller) {
        var cmd = new AppendEventCommand(accountId, req.eventType(), req.amount(), req.currency(),
                req.categoryId(), req.description(), req.transferGroupId(), req.payload(), req.occurredAt());
        return EventResponse.from(eventService.append(cmd, caller.getId()));
    }

    @GetMapping("/events")
    public PageResponse<EventResponse> list(@PathVariable UUID accountId,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @AuthenticationPrincipal AppUserDetails caller) {
        accountService.getOwned(accountId, caller.getId()); // 404/403 before touching events
        var result = eventRepository.findByAccountIdOrderBySequenceNoDesc(
                accountId, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return PageResponse.from(result.map(EventResponse::from));
    }

    @GetMapping("/balance")
    public BalanceResponse balance(@PathVariable UUID accountId, @AuthenticationPrincipal AppUserDetails caller) {
        var account = accountService.getOwned(accountId, caller.getId());
        return new BalanceResponse(account.getId(), account.getBalance(), account.getBaseCurrency());
    }

    @GetMapping("/balance/check")
    public ConsistencyResponse check(@PathVariable UUID accountId, @AuthenticationPrincipal AppUserDetails caller) {
        return new ConsistencyResponse(accountId, eventService.isBalanceConsistent(accountId, caller.getId()));
    }
}