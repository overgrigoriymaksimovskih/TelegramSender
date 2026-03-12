package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.MessageRequest;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.dto.tg_result.Result;
import com.example.telegramadmin.dto.tg_result.Success;
import com.example.telegramadmin.factory.TelegramResultFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.telegramadmin.exceptions.MessageSendingException;

import java.util.List;

@Service
public class BroadcastOrchestrator {
    private final TelegramResultFactory telegramResultFactory;
    private final NotificationService notificationService;

    @Autowired
    public BroadcastOrchestrator(TelegramResultFactory telegramResultFactory, NotificationService notificationService) {
        this.telegramResultFactory = telegramResultFactory;
        this.notificationService = notificationService;
    }
    public void sendMessage(MessageRequest request)  throws MessageSendingException{
        try{
            // Отправляем сообщение и получаем JSON‑ответ
            String jsonResponse = notificationService.crateMainMessage(request);

            // Если мы здесь, значит ответ успешный (200 OK). Парсим его.
            // Преобразуем JSON в Result<Message>
            Result<Message> result = telegramResultFactory.fromTelegramResponse(
                    jsonResponse,
                    Message.class
            );

            // Обрабатываем успешный ответ
            if (result.isSuccess()) {
                // Получаем список пользователей для уведомлений
                List<NotificationRecipientDto> notificationRecipientsDtoList = notificationService.getRecipientsDtoList(6128969029L);
                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L, "Test"));
                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L, "Test2"));
                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L, "Test3"));
                // Копируем отправленное сообщениe всем пользователям
                List<NotificationResultDto> allNotifications = notificationService.sendCopyOfMessageToRecipients(notificationRecipientsDtoList, result);
                // Выбираем тех кто не смог получить копию сообщения
                List<NotificationResultDto> failedNotifications = notificationService.getFailedNotifications(allNotifications);
            }
        }catch (MessageSendingException e){
            // Просто пробрасываем выше исключение, которое пришло из TelegramHttpService
            // Оно уже содержит все детали (код 403, описание Forbidden)
            throw e;
        }
    }
}
