package com.example.telegramadmin.dto;

import com.example.telegramadmin.enums.NotificationStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResultDto {
    private NotificationRecipientDto user;
    private NotificationStatus status;
    private String detailedMessage;
    private LocalDateTime sentAt;

    public NotificationResultDto(NotificationRecipientDto user, NotificationStatus status) {
        this.user = user;
        this.status = status;
        this.detailedMessage = status.getDescription();
        this.sentAt = LocalDateTime.now();
    }
}
