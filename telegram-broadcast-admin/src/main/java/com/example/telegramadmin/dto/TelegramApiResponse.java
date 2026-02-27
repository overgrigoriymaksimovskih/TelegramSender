package com.example.telegramadmin.dto;

// Это корневая модель для ответа Telegram API.
public class TelegramApiResponse {
    private boolean ok;
    private TelegramMessageResult result;

    // Геттеры и сеттеры
    public boolean isOk() {
        return ok;
    }
    public void setOk(boolean ok) { this.ok = ok; }
    public TelegramMessageResult getResult() { return result; }
    public void setResult(TelegramMessageResult result) { this.result = result; }

}



