package com.example.Account2.exception;

import com.example.Account2.dto.ErrorResponse;
import com.example.Account2.type.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHander {

    @ExceptionHandler(AccountException.class)
    public ErrorResponse handlerAccountException(AccountException e){
        log.error("{} is occurred.", e.getErrorCode());
        return new ErrorResponse(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleDataIntergrityViolationException(
            DataIntegrityViolationException e){
        log.error("{} is occurred", e);

        return new ErrorResponse(ErrorCode.INVALID_REQUEST, ErrorCode.INVALID_REQUEST.getDescription());

    }

    @ExceptionHandler(Exception.class)
    public ErrorResponse handleAccountException(Exception e){
        log.error("{} is occurred.", e);
        return new ErrorResponse(ErrorCode.UNEXPECT_ERROR,
                ErrorCode.UNEXPECT_ERROR.getDescription());
    }
}

