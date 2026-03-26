package com.example.telegramadmin.enums;

public enum NotificationStatus {
    SUCCESS("Message delivered successfully"),
    SKIPPED ("User was skipped"),
    ERROR("Unknown error");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}


