package com.example.account.domain;

import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.example.account.type.ErrorCode.INVALID_REQUEST;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Account extends BaseEntity{
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AccountUser accountUser;

    private Long balance;

    private LocalDateTime unRegisteredAt;



    public static Account createAccount(AccountUser user, Long initialBalance, String accountNumber){
        return Account.builder()
                .accountNumber(accountNumber)
                .accountStatus(AccountStatus.IN_USE)
                .balance(initialBalance)
                .accountUser(user)
                .build();

    }

    public void unRegister() {
        accountStatus = AccountStatus.UNREGISTERED;
        unRegisteredAt = LocalDateTime.now();
    }

    public void useBalance(Long amount){
        if(amount > balance)
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);

        balance -= amount;
    }

    public void cancelBalance(Long amount) {
        if(amount < 0)
            throw new AccountException(INVALID_REQUEST);
        this.balance += amount;
    }
}
