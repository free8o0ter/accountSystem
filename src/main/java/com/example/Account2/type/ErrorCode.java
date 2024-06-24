package com.example.Account2.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없습니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    USER_ACCOUNT_UN_MATCHED("계좌주가 일치하지 않습니다."),
    ACCOUNT_ALREADY_UNREGISTERED("계좌가 이미 해지되어 있습니다."),
    ACCOUNT_NOT_EMPTY("계좌의 잔액이 남아있습니다."),
    MAX_ACCOUNT_PER_USER_10("사용자의 계좌가 10좌가 넘었습니다."),

    AMOUNT_EXCEED_BALANCE("거래 금액이 계좌 잔고를 초과합니다."),
    TRANSACTION_NOT_FOUND("해당 거래가 존재하지 않습니다."),
    TRANSACTION_ACCOUNT_UNMATCHED("요청 계좌의 주인과 해당 거래의 계좌주가 일치하지 않습니다."),
    CANCEL_MUST_FULLY("부분 취소는 허용되지 않습니다."),
    TOO_OLD_ORDER_TO_CANCEL("1년이 지난 거래는 취소가 불가능 합니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    UNEXPECT_ERROR("처리되지 않은 에러가 발생했습니다."),
    ACCOUNT_TRANSACTION_LOCK("해당 계좌는 현재 사용중입니다.");

    private final String description;

}
