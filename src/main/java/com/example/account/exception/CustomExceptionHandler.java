package com.example.account.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<ErrorResponse> handleAccountException(AccountException ex, HttpServletRequest request){
        log.info(ex.getErrorMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
                .body(ErrorResponse.createErrorResponse(ex,request.getRequestURI()));

    }

}
