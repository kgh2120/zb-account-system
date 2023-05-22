package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static com.example.account.type.ErrorCode.AMOUNT_EXCEED_BALANCE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {

        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUseBalance(amount, user, account);

//        account.getBalance();


        return TransactionDto.fromEntity(null);
    }

    private void validateUseBalance(Long amount, AccountUser user, Account account) {
        if(!user.getId().equals(account.getAccountUser().getId()))
            throw new AccountException(ErrorCode.ACCOUNT_OWNER_UN_MATCH);
        if(account.getAccountStatus() != AccountStatus.IN_USE)
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        if(account.getBalance() < amount)
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
    }


}
