package com.example.account.dto;

import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTransactionResponse {

    private String accountNumber;
    private TransactionType transactionType;
    private TransactionResultType transactionResult;
    private String transactionId;
    private Long amount;
    private LocalDateTime transactionAt;

    public static QueryTransactionResponse from(TransactionDto dto){
        return QueryTransactionResponse.builder()
                .accountNumber(dto.getAccountNumber())
                .transactionResult(dto.getTransactionResultType())
                .transactionType(dto.getTransactionType())
                .amount(dto.getAmount())
                .transactionAt(dto.getTransactedAt())
                .transactionId(dto.getTransactionId())
                .build();
    }
}
