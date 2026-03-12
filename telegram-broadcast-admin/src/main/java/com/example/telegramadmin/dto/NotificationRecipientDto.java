package com.example.telegramadmin.dto;

import lombok.Data;

@Data
public class NotificationRecipientDto {
    private Long telegramUserId;
    private String firstName;

    // Конструктор для JPQL
    public NotificationRecipientDto(Long telegramUserId, String firstName) {
        this.telegramUserId = telegramUserId;
        this.firstName = firstName;
    }

    // Пустой конструктор
    public NotificationRecipientDto() {}
}
