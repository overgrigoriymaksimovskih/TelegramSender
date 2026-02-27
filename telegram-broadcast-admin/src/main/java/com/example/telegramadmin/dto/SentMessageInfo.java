package com.example.telegramadmin.dto;

public class SentMessageInfo {
    private Long chatId;
    private Integer messageId;

    public SentMessageInfo(Long chatId, Integer messageId) {
        this.chatId = chatId;
        this.messageId = messageId;
    }

    // Геттеры
    public Long getChatId() { return chatId; }
    public Integer getMessageId() { return messageId; }

    // Можно добавить сеттеры, если нужно будет обновлять
}
