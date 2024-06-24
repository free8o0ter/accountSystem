package com.example.Account2.controller;

import com.example.Account2.dto.AccountDto;
import com.example.Account2.dto.CancelBalance;
import com.example.Account2.dto.TransactionDto;
import com.example.Account2.dto.UseBalance;
import com.example.Account2.service.TransactionService;
import com.example.Account2.type.TransactionResultType;
import com.example.Account2.type.TransactionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void successUseBalance() throws Exception {
        //given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1000000000")
                        .transactionType(TransactionType.USE)
                        .balanceSanpshot(8766L)
                        .transactedAt(LocalDateTime.now())
                        .amount(1234L)
                        .transactionId("transactionId")
                        .transactionResultType(TransactionResultType.S)
                        .build());
        //when
        //then
        mockMvc.perform(post("/transaction/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UseBalance.Request(1L, "2000000000", 3000L)
                ))
        ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.amount").value(1234L))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.transactionResult").value("S"));

    }
    @Test
    void successCancelBalance() throws Exception {
        //given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1000000000")
                        .transactionType(TransactionType.USE)
                        .balanceSanpshot(8766L)
                        .transactedAt(LocalDateTime.now())
                        .amount(54321L)
                        .transactionId("transactionIdForCancel")
                        .transactionResultType(TransactionResultType.S)
                        .build());
        //when
        //then
        mockMvc.perform(post("/transaction/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelBalance.Request("transactionIdForCancel", "2000000000", 3000L)
                        ))
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.amount").value(54321L))
                .andExpect(jsonPath("$.transactionId").value("transactionIdForCancel"))
                .andExpect(jsonPath("$.transactionResult").value("S"));

    }


    @Test
    void successGetTransactionByTransactionId() throws Exception {
        //given
        TransactionDto transactionDto = TransactionDto.builder()
                .accountNumber("1000000000")
                .transactionType(TransactionType.USE)
                .transactionResultType(TransactionResultType.S)
                .amount(1000L)
                .balanceSanpshot(20000L)
                .transactionId("transactionIdForQuery")
                .transactedAt(LocalDateTime.now())
                .build();
        given(transactionService.queryTransaction(anyString()))
                .willReturn(transactionDto);
        //when
        //then
        mockMvc.perform(get("/transaction/transactionIdForQuery"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.transactionId").value("transactionIdForQuery"));

    }

}