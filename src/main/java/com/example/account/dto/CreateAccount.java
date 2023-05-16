package com.example.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class CreateAccount {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request{
        private Long id;
        private Long initialBalance;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response{
        private Long id;
        private String accountNumber;
        private LocalDateTime registeredAt;
    }
}
