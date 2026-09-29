package com.marouan.finance_app.service;

import com.marouan.finance_app.domain.Account;
import com.marouan.finance_app.domain.AccountType;
import com.marouan.finance_app.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    // fetches the account and enforces that it belongs to the caller, used by every read/append path
    @Transactional(readOnly = true)
    public Account getOwned(UUID accountId, UUID callerId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found: " + accountId));
        if (!account.getUserId().equals(callerId)) {
            throw new AccessDeniedException("Not your account");
        }
        return account;
    }

    @Transactional
    public Account create(UUID userId, String name, AccountType type, String baseCurrency) {
        // room for real rules later: max accounts per user, duplicate name check, currency whitelist...
        var account = new Account(userId, name, type, baseCurrency);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<Account> listForUser(UUID userId) {
        return accountRepository.findByUserId(userId);
    }
}