package com.example.account.domain;

import com.example.account.type.AccountStatus;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Account extends BaseEntity{
    @Id
    @GeneratedValue
    private Long id;

    private String accountNumber;

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AccountUser accountUser;

    private Long balance;



    public static Account createAccount(AccountUser user, Long initialBalance, String accountNumber){
        return Account.builder()
                .accountNumber(accountNumber)
                .accountStatus(AccountStatus.IN_USE)
                .balance(initialBalance)
                .accountUser(user)
                .build();

    }

}
