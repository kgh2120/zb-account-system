package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.domain.AccountUser;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.account.type.ErrorCode.*;

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
    public AccountDto createAccount(Long id, Long initialBalance) {
        AccountUser user = findUserOrElseThrow(id);

        validateCreateAccount(user);

        return AccountDto.fromEntity(accountRepository
                .save(
                        Account.createAccount(user,
                                initialBalance,
                                createAccountNumber())));
    }

    @Transactional
    public Account getAccount(Long id) {
        if (id < 0) {
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }

    /**
     * 계좌를 해지시킨다.
     * 입력받은 ID를 통해 회원 조회를 진행 후, 계좌 번호로 계좌를 조회한다.
     * 이후 계좌를 해지한다.
     * 이때 계좌가 회원의 소유가 아닌 경우, 잔액이 남은 경우, 이미 해지된 경우엔
     * 계좌 해지를 실패한다.
     * @param userId 회원 ID
     * @param accountNumber 계좌 번호
     * @return AccountDto
     * @throws AccountException USER_NOT_FOUND , ACCOUNT_NOT_FOUND ,
     * ACCOUNT_ALREADY_UNREGISTERED, BALANCE_REMAIN, ACCOUNT_OWNER_UN_MATCH
     *
     */
    @Transactional
    public AccountDto unRegisterAccount(Long userId, String accountNumber) {
        AccountUser accountUser = findUserOrElseThrow(userId);

        Account account = findAccountByAccountNumberOrElseThrow(accountNumber);

        validateUnRegisterAccount(accountUser, account);

        account.unRegister();

        return AccountDto.fromEntity(account);
    }

    public List<AccountDto> getAllAccountInfo(Long userId) {
        AccountUser user = findUserOrElseThrow(userId);
        return accountRepository.findByAccountUser(user)
                .stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }

    private Account findAccountByAccountNumberOrElseThrow(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));
    }

    private void validateUnRegisterAccount(AccountUser accountUser, Account account) {
        if(!account.getAccountUser().getId().equals(accountUser.getId()))
            throw new AccountException(ACCOUNT_OWNER_UN_MATCH);
        if(account.getBalance() > 0)
            throw new AccountException(BALANCE_REMAIN);
        if(account.getAccountStatus().equals(AccountStatus.UNREGISTERED))
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
    }

    private void validateCreateAccount(AccountUser user) {
        if(accountRepository.countByAccountUser(user) >= 10)
            throw new AccountException(ErrorCode.EXCEED_MAX_ACCOUNT_SIZE);
    }

    private String createAccountNumber() {

        return accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())+1+""))
                .orElse(INITIAL_ACCOUNT_NUMBER);
    }

    private AccountUser findUserOrElseThrow(Long id) {
        return accountUserRepository.findById(id)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));
    }
}
