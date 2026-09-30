package com.marouan.finance_app.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// one line pulled out of the PDF, nothing written to the DB yet
// rawLine stays so the user can see what we parsed it from, in case the guess is wrong
public record ProposedEvent(BigDecimal amount, LocalDate date, String description, String rawLine) {}