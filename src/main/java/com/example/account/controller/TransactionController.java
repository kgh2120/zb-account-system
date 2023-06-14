package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public ResponseEntity<UseBalance.Response> useBalance(
            @Valid @RequestBody UseBalance.Request request
    ){
        try {
            return ResponseEntity.ok(UseBalance.Response
                    .from(transactionService
                            .useBalance(request.getUserId(),
                                    request.getAccountNumber(),
                                    request.getAmount())));
        } catch (AccountException e) {
            log.error("Failed to use Balance");

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @PostMapping("/transaction/cancel")
    public ResponseEntity<CancelBalance.Response> cancelBalance(
            @Valid @RequestBody CancelBalance.Request request
    ){
        try {
            return ResponseEntity.ok(CancelBalance.Response
                    .from(transactionService
                            .cancelBalance(request.getTransactionId(),
                                    request.getAccountNumber(),
                                    request.getAmount())));
        } catch (AccountException e) {
            log.error("Failed to use Balance");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<QueryTransactionResponse> queryTransaction(
            @PathVariable String transactionId
    ){
        return ResponseEntity.ok(QueryTransactionResponse
                .from(transactionService
                        .queryTransaction(transactionId)));


    }

}
