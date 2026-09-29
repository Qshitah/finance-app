package com.marouan.finance_app.web;

import com.marouan.finance_app.security.AppUserDetails;
import com.marouan.finance_app.service.AccountService;
import com.marouan.finance_app.service.CategoryService;
import com.marouan.finance_app.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountManagementController {

    private final AccountService accountService;
    private final CategoryService categoryService;

    @PostMapping("/api/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest req,
                                         @AuthenticationPrincipal AppUserDetails caller) {
        var account = accountService.create(caller.getId(), req.name(), req.type(), req.baseCurrency());
        return AccountResponse.from(account);
    }

    @GetMapping("/api/accounts")
    public List<AccountResponse> listAccounts(@AuthenticationPrincipal AppUserDetails caller) {
        return accountService.listForUser(caller.getId()).stream().map(AccountResponse::from).toList();
    }

    @PostMapping("/api/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest req,
                                           @AuthenticationPrincipal AppUserDetails caller) {
        var category = categoryService.create(caller.getId(), req.name(), req.type());
        return CategoryResponse.from(category);
    }

    @GetMapping("/api/categories")
    public List<CategoryResponse> listCategories(@AuthenticationPrincipal AppUserDetails caller) {
        return categoryService.listForUser(caller.getId()).stream().map(CategoryResponse::from).toList();
    }
}