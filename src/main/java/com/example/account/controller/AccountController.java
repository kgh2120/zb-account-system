package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.service.AccountService;
import com.example.account.service.RedisTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
//    private final RedisTestService redisTestService;

//    @GetMapping("/get-lock")
//    public String getLock() {
//        return redisTestService.getLock();
//    }

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

    @GetMapping("/account/{id}")
    public Account getAccount(
            @PathVariable Long id){
        return accountService.getAccount(id);
    }
}
