package com.marouan.finance_app.repository;

import com.marouan.finance_app.domain.AccountEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountEventRepository extends JpaRepository<AccountEvent, UUID> {

    // last position used in this account, 0 if it has no events yet
    @Query("select coalesce(max(e.sequenceNo), 0) from AccountEvent e where e.accountId = :accountId")
    long findMaxSequenceNo(@Param("accountId") UUID accountId);

    // newest first, paged so a big account doesn't load everything
    Page<AccountEvent> findByAccountIdOrderBySequenceNoDesc(UUID accountId, Pageable pageable);

    // the V1-style replay, I'll use it to check the cached balance hasn't drifted
    @Query("select coalesce(sum(e.amount), 0) from AccountEvent e where e.accountId = :accountId")
    BigDecimal replayBalance(@Param("accountId") UUID accountId);
}