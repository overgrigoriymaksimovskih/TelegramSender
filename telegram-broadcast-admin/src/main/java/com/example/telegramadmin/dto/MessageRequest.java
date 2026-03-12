package com.example.telegramadmin.dto;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.web.multipart.MultipartFile;

public class MessageRequest {

    /** Текст сообщения (может быть пустым) */
    private String text;

    /** Файл фото (может быть null) */
    private MultipartFile photo;

    // Кастомная проверка: хотя бы одно поле должно быть заполнено
    @AssertTrue(message = "Сообщение не может быть пустым. Заполните текст или прикрепите изображение.")
    public boolean isContentPresent() {
        return (text != null && !text.trim().isEmpty()) || (photo != null && !photo.isEmpty());
    }

    // Getters & Setters -----------------------------------------------------------------------------------------------
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public MultipartFile getPhoto() { return photo; }
    public void setPhoto(MultipartFile photo) { this.photo = photo; }

}
