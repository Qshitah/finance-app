package com.marouan.finance_app.service;

import com.marouan.finance_app.domain.Account;
import com.marouan.finance_app.domain.AccountEvent;
import com.marouan.finance_app.repository.AccountEventRepository;
import com.marouan.finance_app.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final AccountRepository accountRepository;
    private final AccountEventRepository eventRepository;

    @Transactional
    public AccountEvent append(AppendEventCommand cmd) {
        // lock the account row first, anyone else appending to it waits here until we commit
        Account account = accountRepository.findByIdForUpdate(cmd.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + cmd.accountId()));

        // for now one currency per account, otherwise the cached balance would add EUR to USD
        // (we'll decide how to really handle multi-currency later)
        if (!account.getBaseCurrency().equals(cmd.currency())) {
            throw new IllegalArgumentException(
                    "Account is " + account.getBaseCurrency() + " but event is " + cmd.currency());
        }

        // safe to read the max now, the lock guarantees nobody sneaks in between
        long nextSeq = eventRepository.findMaxSequenceNo(account.getId()) + 1;

        AccountEvent event = new AccountEvent(
                account.getId(), nextSeq, cmd.eventType(), cmd.amount(), cmd.currency(),
                cmd.categoryId(), cmd.description(), cmd.transferGroupId(),
                cmd.payload(), cmd.occurredAt());

        // flush now so a constraint problem shows up here and not at commit time
        AccountEvent saved = eventRepository.saveAndFlush(event);

        // same transaction: if this fails, the event insert rolls back too
        account.applyDelta(cmd.amount());

        return saved;
    }

    // recompute from events and compare with the cached balance, true = no drift
    @Transactional(readOnly = true)
    public boolean isBalanceConsistent(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
        BigDecimal replayed = eventRepository.replayBalance(accountId);
        return account.getBalance().compareTo(replayed) == 0;
    }
}