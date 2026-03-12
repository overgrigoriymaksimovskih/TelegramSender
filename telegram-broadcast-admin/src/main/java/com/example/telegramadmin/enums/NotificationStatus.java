package com.example.telegramadmin.enums;

public enum NotificationStatus {
    SUCCESS("Message delivered successfully"),
    USER_BLOCKED("User blocked the bot"),
    USER_NOT_FOUND("User not found"),
    BOT_BLOCKED("Bot was blocked by user"),
    NETWORK_ERROR("Network error occurred"),
    TELEGRAM_API_ERROR("Telegram API error"),
    ERROR("Unknown error");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


