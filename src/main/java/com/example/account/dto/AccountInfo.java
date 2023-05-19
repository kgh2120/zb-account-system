package com.example.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class AccountInfo {

    private String accountNumber;
    private Long balance;

    public static AccountInfo from(AccountDto accountDto) {
        return AccountInfo.builder()
                .accountNumber(accountDto.getAccountNumber())
                .balance(accountDto.getBalance())
                .build();
    }
}
