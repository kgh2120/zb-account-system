package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("사용자가 없습니다.", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND("입력한 계좌 번호에 해당하는 계좌가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ACCOUNT_TRANSACTION_LOCK("해당 계좌는 사용중입니다.", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND("입력한 거래 번호에 해당하는 거래가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    TRANSACTION_ACCOUNT_UN_MATCH("거래와 계좌번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    TRANSACTION_AMOUNT_UN_MATCH("거래 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    TOO_OLD_TRANSACTION_TO_CANCEL("1년이 지난 거래는 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    ACCOUNT_OWNER_UN_MATCH("계좌의 소유주가 아닙니다.", HttpStatus.UNAUTHORIZED),
    BALANCE_REMAIN("계좌에 잔액이 남아있습니다.", HttpStatus.BAD_REQUEST),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지되었습니다.", HttpStatus.BAD_REQUEST),

    EXCEED_MAX_ACCOUNT_SIZE("계좌 보유량 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),

    AMOUNT_EXCEED_BALANCE("거래 금액이 잔액보다 큽니다", HttpStatus.BAD_REQUEST)


    ;

    private final String description;
    private final HttpStatus httpStatus;
}
