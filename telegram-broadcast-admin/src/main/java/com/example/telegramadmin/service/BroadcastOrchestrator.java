package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.TelegramApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.telegramadmin.service.TelegramHttpService;

@Service
public class BroadcastOrchestrator {

    private final TelegramHttpService telegramHttpService;

    @Autowired
    public BroadcastOrchestrator(TelegramHttpService telegramHttpService) {
        this.telegramHttpService = telegramHttpService;
    }
    public void sendMessage(String caption, MultipartFile photoFile) {
        if (photoFile == null || photoFile.isEmpty()) {
            System.err.println("Файл изображения не выбран или пуст.");
            return;
        }
        telegramHttpService.sendPhotoMessage(caption, photoFile);
    }
    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.err.println("Текст сообщения пуст.");
            return;
        }

        System.out.println(telegramHttpService.sendTextMessage(text).isOk());
    }
}
