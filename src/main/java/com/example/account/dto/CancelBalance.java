package com.example.account.dto;

import com.example.account.aop.AccountLockId;
import com.example.account.type.TransactionResultType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

public class CancelBalance {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request implements AccountLockId {
        @NotNull
        @Size(min = 32, max = 32)
        private String transactionId;

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber;

        @NotNull
        @Min(10)
        @Max(1000_000_000)
        private Long amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response{
        private String accountNumber;
        private TransactionResultType transactionResult;
        private String transactionId;
        private Long amount;
        private LocalDateTime transactionAt;

        public static Response from(TransactionDto dto){
            return Response.builder()
                    .accountNumber(dto.getAccountNumber())
                    .transactionResult(dto.getTransactionResultType())
                    .amount(dto.getAmount())
                    .transactionAt(dto.getTransactedAt())
                    .transactionId(dto.getTransactionId())
                    .build();
        }


    }
}
