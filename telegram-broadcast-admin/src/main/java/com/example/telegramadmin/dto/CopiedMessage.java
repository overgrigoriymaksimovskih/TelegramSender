package com.example.telegramadmin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CopiedMessage {
    @JsonProperty("message_id")
    private Long messageId;

    // Обязательные геттеры и сеттеры для Jackson
    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "CopiedMessage{messageId=" + messageId + "}";
    }
}