package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountInfo;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.UnRegisterAccount;
import com.example.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;


    @PostMapping("/account")
    public ResponseEntity<CreateAccount.Response> createAccount(@RequestBody @Valid CreateAccount.Request request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateAccount.Response.from(
                        accountService
                                .createAccount(
                                        request.getUserId(),
                                        request.getInitialBalance())));
    }

    @DeleteMapping("/account")
    public ResponseEntity<UnRegisterAccount.Response> unregisterAccount(
            @RequestBody @Valid UnRegisterAccount.Request request
    ) {
        return ResponseEntity
                .ok(UnRegisterAccount.Response
                        .from(accountService
                                .unRegisterAccount(request.getUserId(),
                                        request.getAccountNumber())));

    }
    @GetMapping("/account")
    public ResponseEntity<List<AccountInfo>> getAllAccountInfo(@RequestParam("user_id") long userId){
        return ResponseEntity.ok(
                accountService
                .getAllAccountInfo(userId)
                .stream()
                .map(AccountInfo::from)
                .collect(Collectors.toList()));
    }

    @GetMapping("/account/{id}")
    public Account getAccount(
            @PathVariable Long id) {
        return accountService.getAccount(id);
    }
}
