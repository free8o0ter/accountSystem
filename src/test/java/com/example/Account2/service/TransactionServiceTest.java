package com.example.Account2.service;

import com.example.Account2.domain.Account;
import com.example.Account2.domain.AccountUser;
import com.example.Account2.domain.Transaction;
import com.example.Account2.dto.TransactionDto;
import com.example.Account2.exception.AccountException;
import com.example.Account2.repository.AccountRepository;
import com.example.Account2.repository.AccountUserRepository;
import com.example.Account2.repository.TransactionRepository;
import com.example.Account2.type.AccountStatus;
import com.example.Account2.type.ErrorCode;
import com.example.Account2.type.TransactionResultType;
import com.example.Account2.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .balanceSanpshot(9000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000",
                1000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(9000L, captor.getValue().getBalanceSanpshot());
        assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(9000L, transactionDto.getBalanceSanpshot());
    }

    @Test
    @DisplayName("해당 유저 없음 : 잔액 사용 실패")
    void useBalance_userNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(2000L,
                        "1000000000",
                        1000L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 실패")
    void useBalance_AccountNotFound() {

        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L,"1000000000", 3214L));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }



    @Test
    @DisplayName("계좌 소유주 불일치 - 잔액 사용 실패")
    void useBalanceFailed_userUnMatched() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        AccountUser otherUser = AccountUser.builder()
                .id(13L)
                .name("hipi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));


        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 321L));

        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCHED, accountException.getErrorCode());
    }


    @Test
    @DisplayName("해지된 계좌 요청 - 잔액 사용 실패")
    void useBalanceFailed_accountAlreadyUnRegistered() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(1000L)
                        .accountNumber("1000000012")
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));


        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 321L));

        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }



    @Test
    @DisplayName("거래 금액 잔액 초과")
    void useBalanceFailed_overBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .balance(100L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));


        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 321L));
        //then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, accountException.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }


    @Test
    @DisplayName("잔액 사용 실패시 데이터 저장")
    void saveFailedUseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .balance(10000L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .balanceSanpshot(9000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        transactionService.saveFailedUseTransaction(account.getAccountNumber(),1000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSanpshot());
        assertEquals(TransactionResultType.F, captor.getValue().getTransactionResultType());
    }



    @Test
    void successCancelBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        Account account = Account.builder()
                .accountUser(user)
                .balance(9000L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.CANCEL)
                .transactionResultType(TransactionResultType.S)
                .amount(1000L)
                .balanceSanpshot(9000L)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now())
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(TransactionType.CANCEL)
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .balanceSanpshot(10000L)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .build());
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.cancelBalance("transactionId", "1000000000",
                1000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSanpshot());
        assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
        assertEquals(TransactionType.CANCEL, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(10000L, transactionDto.getBalanceSanpshot());
    }


    @Test
    @DisplayName("해당 거래 없음 - 사용 취소 실패")
    void cancelBalance_TransactionNotFound() {

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("testTransactionId","1000000000", 3214L));

        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
    }


    @Test
    @DisplayName("해당 계좌 없음 - 사용 취소 실패")
    void cancelBalance_AccountNotFound() {


        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder().build()));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionIdForCancel","1000000000", 3214L));

        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }


    @Test
    @DisplayName("계좌 소유주 불일치 - 사용 취소 실패")
    void cancelBalanceFailed_TransactionAccountUnMatched() {
        //given

        AccountUser otherUser = AccountUser.builder()
                .id(13L)
                .name("hipi")
                .build();
        Account otherAccount = Account.builder()
                .id(13L)
                .accountUser(otherUser)
                .balance(9000L)
                .accountNumber("1000000013")
                .accountStatus(AccountStatus.IN_USE)
                .build();

        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        Account account = Account.builder()
                .id(15L)
                .accountUser(user)
                .balance(9000L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.CANCEL)
                .transactionResultType(TransactionResultType.S)
                .amount(1000L)
                .balanceSanpshot(9000L)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now())
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(otherAccount));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionIdForCancel","1000000000", 3214L));

        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UNMATCHED, accountException.getErrorCode());
    }


    @Test
    @DisplayName("부분 취소 시도 - 사용 취소 실패")
    void cancelBalanceFailed_CancelMustFully() {
        //given

       AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        Account account = Account.builder()
                .id(15L)
                .accountUser(user)
                .balance(9000L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.CANCEL)
                .transactionResultType(TransactionResultType.S)
                .amount(1000L)
                .balanceSanpshot(9000L)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now())
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionIdForCancel","1000000000", 3214L));

        //then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, accountException.getErrorCode());
    }


    @Test
    @DisplayName("오래된 거래 취소 - 사용 취소 실패")
    void cancelBalanceFailed_TimeOut() {
        //given

        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        Account account = Account.builder()
                .id(15L)
                .accountUser(user)
                .balance(9000L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.CANCEL)
                .transactionResultType(TransactionResultType.S)
                .amount(3214L)
                .balanceSanpshot(9000L)
                .transactionId("transactionIdForCancel")
                .transactedAt(LocalDateTime.now().minusYears(2L))
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionIdForCancel","1000000000", 3214L));

        //then
        assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, accountException.getErrorCode());
    }

    @Test
    void queryTransaction_Success() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("pobi")
                .build();
        Account account = Account.builder()
                .id(15L)
                .accountUser(user)
                .balance(9000L)
                .accountNumber("1000000012")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder()
                                .id(1L)
                                .transactionType(TransactionType.USE)
                                .transactionResultType(TransactionResultType.S)
                                .account(account)
                                .amount(2000L)
                                .balanceSanpshot(50000L)
                                .transactionId("transactionId")
                                .transactedAt(LocalDateTime.now())
                        .build()));
        //when
        TransactionDto transactionDto = transactionService.queryTransaction("transactionId");
        //then
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
        assertEquals(2000L, transactionDto.getAmount());
    }


    @Test
    @DisplayName("해당 거래 없음 - 사용 취소 실패")
    void queryTransaction_TransactionNotFound() {

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction("testTransactionId"));

        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
    }






}