package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("사용자가 없습니다.", HttpStatus.NOT_FOUND),
    EXCEED_MAX_ACCOUNT_SIZE("계좌 보유량 한도를 초과했습니다.", HttpStatus.BAD_REQUEST)


    ;

    private final String description;
    private final HttpStatus httpStatus;
}
