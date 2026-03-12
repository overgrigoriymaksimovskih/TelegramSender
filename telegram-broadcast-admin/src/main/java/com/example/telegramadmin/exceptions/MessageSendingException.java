package com.example.telegramadmin.exceptions;

// Расширяем Exception, а не RuntimeException, чтобы сделать его проверяемым (checked)
public class MessageSendingException extends Exception {

    // Конструктор с сообщением об ошибке
    public MessageSendingException(String message) {
        super(message);
    }

    // Конструктор с сообщением и причиной (другим исключением)
    public MessageSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
