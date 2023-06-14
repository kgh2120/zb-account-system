package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.TransactionDto;
import com.example.account.dto.UseBalance;
import com.example.account.service.TransactionService;
import com.example.account.type.TransactionResultType;
import com.example.account.type.TransactionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void successUseBalance  () throws Exception{
        //given
        given(transactionService.useBalance(anyLong(),anyString(),anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234123412")
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .transactionId("transactionId")
                        .build());
        //when
        UseBalance.Request request = new UseBalance.Request(1L, "1234123412", 1000L);

        mockMvc.perform(post("/transaction/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234123412"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.amount").value(1000L));


        //then
        
    
    }
    @Test
    void successCancelBalance  () throws Exception{
        //given
        String transactionId = UUID.randomUUID().toString().replace("-","");
        given(transactionService.cancelBalance(anyString(),anyString(),anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234123412")
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .transactionId(transactionId)
                        .build());
        //when
        CancelBalance.Request request = new CancelBalance.Request(transactionId, "1234123412", 1000L);

        mockMvc.perform(post("/transaction/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234123412"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.amount").value(1000L));
    }
    @Test
    void successQueryTransaction() throws Exception{
        //given
        String transactionId = UUID.randomUUID().toString().replace("-","");
        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234123412")
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .transactionId(transactionId)
                        .build());
        //when


        mockMvc.perform(get("/transaction/"+transactionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234123412"))
                .andExpect(jsonPath("$.transactionResult").value("S"))
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.amount").value(1000L))
                .andExpect(jsonPath("$.transactionType").value("USE"));
    }
}