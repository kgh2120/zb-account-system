package com.example.account.controller;

import com.example.account.domain.Account;
import com.example.account.dto.AccountDto;
import com.example.account.dto.CreateAccount;
import com.example.account.dto.UnRegisterAccount;
import com.example.account.type.AccountStatus;
import com.example.account.service.AccountService;
import com.example.account.service.LockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @MockBean
    private AccountService accountService;

    @MockBean
    private LockService lockService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Test
    void successCreateAccount  () throws Exception{
        //given
        given(accountService.createAccount(anyLong(),anyLong()))
                .willReturn(AccountDto.builder()
                        .accountNumber("12345")
                        .userId(1L)
                        .createdAt(LocalDateTime.now())
                        .balance(1000000L)
                        .build());
        //when

        //then
        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateAccount.Request(1L, 10000L))))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.id").value(1L));
    }
    @Test
    void successUnRegisterAccount  () throws Exception{
        //given
        given(accountService.unRegisterAccount(anyLong(), anyString()))
                .willReturn(AccountDto.builder()
                        .accountNumber("1000000000")
                        .userId(1L)
                        .createdAt(LocalDateTime.now())
                        .balance(1000000L)
                        .build());
        //when

        //then
        mockMvc.perform(delete("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(new UnRegisterAccount
                                        .Request(1L, "1000000000"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.id").value(1L));
    }
    @Test
    void successGetAllAccount() throws Exception {
        //given

        given(accountService.getAllAccountInfo(anyLong()))
                .willReturn(List.of(
                        AccountDto.builder()
                                .accountNumber("1111111111")
                                .balance(1000L)
                                .build(),
                        AccountDto.builder()
                                .accountNumber("2222222222")
                                .balance(2000L)
                                .build(),
                        AccountDto.builder()
                                .accountNumber("3333333333")
                                .balance(3000L)
                                .build()
                ));

        //when
        //then
        mockMvc.perform(get("/account?user_id=1"))
                .andDo(print())
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$.[0].accountNumber").value("1111111111"))
                .andExpect(jsonPath("$.[0].balance").value(1000))
                .andExpect(jsonPath("$.[1].accountNumber").value("2222222222"))
                .andExpect(jsonPath("$.[1].balance").value(2000))
                .andExpect(jsonPath("$.[2].accountNumber").value("3333333333"))
                .andExpect(jsonPath("$.[2].balance").value(3000))
                .andExpect(status().isOk());
    }


    @Test
    void successGetAccount() throws Exception {
        //given
        given(accountService.getAccount(anyLong()))
                .willReturn(Account.builder()
                        .accountNumber("3456")
                        .accountStatus(AccountStatus.IN_USE)
                        .build());

        //when
        //then
        mockMvc.perform(get("/account/876"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("3456"))
                .andExpect(jsonPath("$.accountStatus").value("IN_USE"))
                .andExpect(status().isOk());
    }
}