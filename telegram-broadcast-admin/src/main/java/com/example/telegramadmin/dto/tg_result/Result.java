package com.example.telegramadmin.dto.tg_result;

public sealed interface Result<T> permits Success, Failure {
    boolean isSuccess();
    boolean isFailure();
}