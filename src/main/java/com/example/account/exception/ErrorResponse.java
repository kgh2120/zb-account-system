package com.example.account.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {

    private LocalDateTime timeStamp;
    private HttpStatus httpStatus;
    private String errorName;
    private String errorMessage;
    private String path;

    public static ErrorResponse createErrorResponse(AccountException ex, String path){
        return ErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .httpStatus(ex.getErrorCode().getHttpStatus())
                .errorName(ex.getErrorCode().name())
                .errorMessage(ex.getErrorMessage())
                .path(path)
                .build();
    }
}
