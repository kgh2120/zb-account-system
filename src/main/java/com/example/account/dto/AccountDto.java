package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AccountDto {

    private Long userId;
    private String accountNumber;
    private Long balance;
    private LocalDateTime createdAt;
    private LocalDateTime unRegisteredAt;

    public static AccountDto fromEntity(Account account) {
        return AccountDto.builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .createdAt(account.getCreatedAt())
                .balance(account.getBalance())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }

}
