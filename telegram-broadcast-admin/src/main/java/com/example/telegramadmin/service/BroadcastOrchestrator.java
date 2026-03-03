package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.tg_result.Result;
import com.example.telegramadmin.dto.tg_result.Success;
import com.example.telegramadmin.dto.tg_result.Failure;
import com.example.telegramadmin.factory.TelegramResultFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
public class BroadcastOrchestrator {

    private final TelegramHttpService telegramHttpService;
    private final TelegramResultFactory telegramResultFactory;

    @Autowired
    public BroadcastOrchestrator(TelegramHttpService telegramHttpService, TelegramResultFactory telegramResultFactory) {
        this.telegramHttpService = telegramHttpService;
        this.telegramResultFactory = telegramResultFactory;
    }
    public void sendMessage(String caption, MultipartFile photoFile) {
        if (photoFile == null || photoFile.isEmpty()) {
            System.err.println("Файл изображения не выбран или пуст.");
            return;
        }
        //

    }
    public void sendMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.err.println("Текст сообщения пуст.");
            return;
        }
        // 1️⃣ Отправляем сообщение и получаем JSON‑ответ
        String jsonResponse = telegramHttpService.sendTextMessage(text);

        // 2️⃣ Преобразуем JSON в Result<Message>
        Result<Message> result = telegramResultFactory.fromTelegramResponse(
                jsonResponse,
                Message.class
        );

        // 3️⃣ Обрабатываем успешный ответ
        if (result.isSuccess()) {
            // Внутри Success<T> хранится объект Message
            Message message = ((Success<Message>) result).getValue();

            // Пример: выводим ID отправленного сообщения
            System.out.println("Message sent! ID = " + message.getMessageId());

            // Здесь можно делать всё, что нужно с Message (репост, логирование и т.д.)
            return;
        }

        // 4️⃣ Обрабатываем ошибку
        Failure<Message> failure = (Failure<Message>) result;

        // Если ошибка пришла от Telegram (ok:false)
        if (failure.getErrorCode() != null) {
            System.err.println("Telegram error: code " + failure.getErrorCode()
                    + ", description: " + failure.getDescription());
        }
        // Если это локальная ошибка (например, пустой текст)
        else if (failure.getException() != null) {
            System.err.println("Local error: " + failure.getException().getMessage());
        }
        // Любая другая непредвиденная ситуация
        else {
            System.err.println("Unknown error while sending message");
        }
    }
}
