package com.example.telegramadmin.dto;

// Модель для содержимого 'result'.
// Структура 'result' для TEXT и PHOTO ответов вроде одинаковая...
public class TelegramMessageResult {
    private Integer message_id;
    private TelegramChat chat;

// Геттеры и сеттеры
    public Integer getMessage_id() { return message_id; }
    public void setMessage_id(Integer message_id) { this.message_id = message_id; }
    public TelegramChat getChat() { return chat; }
    public void setChat(TelegramChat chat) { this.chat = chat; }
}
