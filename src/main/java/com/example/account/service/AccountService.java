package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.dto.CreateAccount;
import com.example.account.exception.AccountException;
import com.example.account.type.AccountStatus;
import com.example.account.domain.AccountUser;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    private static final String INITIAL_ACCOUNT_NUMBER = "1000000000";

    /**
     * id에 해당하는 사용자를 조회
     * 계좌 번호를 생성하고
     * 사용자 계정에 계좌를 저장하고
     * 생성된 계좌의 정보를 전달한다.
     *
     * @param id 사용자 아이디
     * @param initialBalance 초기 잔액
     * @throws AccountException if id not founded
     */
    @Transactional
    public CreateAccount.Response createAccount(Long id, Long initialBalance) {
        AccountUser user = accountUserRepository.findById(id)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));


        Account account = Account.createAccount(user,initialBalance,createAccountNumber());
        accountRepository.save(account);

        return CreateAccount.Response.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .registeredAt(account.getCreatedAt())
                .build();

    }

    private String createAccountNumber() {

        return accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())+1+""))
                .orElse(INITIAL_ACCOUNT_NUMBER);
    }

    @Transactional
    public Account getAccount(Long id) {
        if (id < 0) {
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }
}
