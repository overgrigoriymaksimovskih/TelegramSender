package com.example.telegramadmin.dto;

import com.example.telegramadmin.dto.tg_result.Result;

public class TelegramApiResponse {
    private boolean ok;
    private CopiedMessage result;
    private String description;
    private Integer errorCode;

    // Геттеры и сеттеры обязательны для Jackson
    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public CopiedMessage getResult() {
        return result;
    }

    public void setResult(CopiedMessage result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    // Метод для преобразования в систему Result
    public Result<CopiedMessage> toResult() {
        if (ok) {
            return new com.example.telegramadmin.dto.tg_result.Success<>(result);
        } else {
            return new com.example.telegramadmin.dto.tg_result.Failure<>(errorCode, description);
        }
    }
}
