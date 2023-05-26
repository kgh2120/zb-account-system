package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.example.account.type.ErrorCode.*;

@Slf4j
@Service
@Transactional
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

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveTransaction(amount, account, TransactionResultType.S, TransactionType.USE));
    }

    private void validateUseBalance(Long amount, AccountUser user, Account account) {
        if(!user.getId().equals(account.getAccountUser().getId()))
            throw new AccountException(ErrorCode.ACCOUNT_OWNER_UN_MATCH);
        if(account.getAccountStatus() != AccountStatus.IN_USE)
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        if(account.getBalance() < amount)
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
    }


    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveTransaction(amount, account, TransactionResultType.F, TransactionType.USE);
    }

    private Transaction saveTransaction(Long amount, Account account, TransactionResultType resultType, TransactionType transactionType) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(transactionType)
                        .transactionResultType(resultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactedAt(LocalDateTime.now())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .build()
        );
    }

    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount) {

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCancelBalance(amount, transaction, account);

        account.cancelBalance(amount);

        return TransactionDto.fromEntity(saveTransaction(amount, account, TransactionResultType.S, TransactionType.CANCEL));
    }

    private void validateCancelBalance(Long amount, Transaction transaction, Account account) {
        if(!transaction.getAccount().getId().equals(account.getId()))
            throw new AccountException(TRANSACTION_ACCOUNT_UN_MATCH);
        if(!transaction.getAmount().equals(amount))
            throw new AccountException(TRANSACTION_AMOUNT_UN_MATCH);
        if(transaction.getTransactedAt().plusYears(1).isBefore(LocalDateTime.now()))
            throw new AccountException(TOO_OLD_TRANSACTION_TO_CANCEL);
        if(amount < 0)
            throw new AccountException(INVALID_REQUEST);
    }

    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveTransaction(amount, account, TransactionResultType.F, TransactionType.CANCEL);
    }
}
