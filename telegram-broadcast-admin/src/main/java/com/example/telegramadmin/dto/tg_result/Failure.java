package com.example.telegramadmin.dto.tg_result;

public final class Failure<T> implements Result<T> {
    private final Integer errorCode;
    private final String description;
    private final Exception exception;

    public Failure(Integer errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
        this.exception = null;
    }

    public Failure(Exception exception) {
        this.errorCode = null;
        this.description = null;
        this.exception = exception;
    }

    public Integer getErrorCode() { return errorCode; }
    public String getDescription() { return description; }
    public Exception getException() { return exception; }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return true;
    }
}