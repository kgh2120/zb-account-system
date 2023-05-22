package com.example.account.controller;

import com.example.account.dto.UseBalance;
import com.example.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TransactionController {

    private TransactionService transactionService;

    @PostMapping("/transaction/use")
    public ResponseEntity<UseBalance.Response> useBalance(
            @Valid @RequestBody UseBalance.Request request
    ){


        transactionService.useBalance(request.getUserId(),
                request.getAccountNumber(),
                request.getAmount());
    }

}
