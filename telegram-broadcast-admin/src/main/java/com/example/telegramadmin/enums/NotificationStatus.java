package com.example.telegramadmin.enums;

public enum NotificationStatus {
    SUCCESS("Message delivered successfully"),
    ERROR("Unknown error");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


