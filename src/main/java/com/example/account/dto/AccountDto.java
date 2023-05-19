package com.example.account.dto;

import com.example.account.domain.Account;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountDto that = (AccountDto) o;
        return Objects.equals(userId, that.userId) && Objects.equals(accountNumber, that.accountNumber) && Objects.equals(balance, that.balance) && Objects.equals(createdAt, that.createdAt) && Objects.equals(unRegisteredAt, that.unRegisteredAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, accountNumber, balance, createdAt, unRegisteredAt);
    }
}
