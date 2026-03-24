package com.example.telegramadmin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.telegramadmin.dto.MessageRequest;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.dto.tg_result.Result;
import com.example.telegramadmin.dto.tg_result.Success;
import com.example.telegramadmin.factory.TelegramResultFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.example.telegramadmin.exceptions.MessageSendingException;

import java.util.ArrayList;
import java.util.List;

@Service
public class BroadcastOrchestrator {
    private final TelegramResultFactory telegramResultFactory;
    private final MessageCopierService messageCopierService;
    private final MessageSenderService messageSenderService;
    private final RecipientService recipientService;


    @Autowired
    public BroadcastOrchestrator(TelegramResultFactory telegramResultFactory, MessageCopierService messageCopierService, MessageSenderService messageSenderService, RecipientService recipientService) {
        this.telegramResultFactory = telegramResultFactory;
        this.messageCopierService = messageCopierService;
        this.messageSenderService = messageSenderService;
        this.recipientService = recipientService;
    }
    public List<NotificationResultDto> sendMessage(MessageRequest request)  throws MessageSendingException{
        try{
            // Отправляем сообщение и получаем JSON‑ответ
            String jsonResponse = messageSenderService.sendMessage(request);

            // Если мы здесь, значит ответ успешный (200 OK). Парсим его.
            // Преобразуем JSON в Result<Message>
            Result<Message> result = telegramResultFactory.fromTelegramResponse(
                    jsonResponse,
                    Message.class
            );
            // Внутри result хранится объект Message
            Message message = ((Success<Message>) result).getValue();

            // Обрабатываем успешный ответ
            if (result.isSuccess()) {
                List<NotificationRecipientDto> notificationRecipientsDtoList = new ArrayList<>();
                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L,"testing recipient 1"));
                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L,"testing recipient 2"));
                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L,"testing recipient 3"));
//                notificationRecipientsDtoList.add(new NotificationRecipientDto(610200129L,"testing recipient 2"));


                List<NotificationResultDto> allNotifications = messageCopierService.copyMessage(notificationRecipientsDtoList, message.getChatId(), message.getMessageId());
                return allNotifications;
//                for(NotificationResultDto dto:allNotifications){
//                    System.out.println(dto.getStatus() + " -> " + dto.getUser().getTelegramUserId() + " -> " + dto.getUser().getFirstName() + " -> " + dto.getDetailedMessage());
//                }
//                        // Получаем список пользователей для уведомлений
//                List<NotificationRecipientDto> notificationRecipientsDtoList = notificationService.getRecipientsDtoList();
//                for(NotificationRecipientDto recipient :notificationRecipientsDtoList){
//                    System.out.println(recipient.getFirstName() + " " + recipient.getTelegramUserId());
//                }
//                // Копируем отправленное сообщениe всем пользователям
//                List<NotificationResultDto> allNotifications = notificationService.sendCopyOfMessageToRecipients(notificationRecipientsDtoList, message);
//                // Выбираем тех кто не смог получить копию сообщения
//                List<NotificationResultDto> failedNotifications = notificationService.getFailedNotifications(allNotifications);
//                // ВОТ ТУТ НУЖНО ВЕРНУТЬ В КОНТРОЛЛЕР ИД ЮЗЕРА НОМЕР ОШИБКИ И ДЕСКРИПШН
//                return failedNotifications;
            } else {
                // Обработка случая, если основное сообщение не отправилось
                //todo тут будет объект failure ? че с ним делать
                throw new MessageSendingException("Failed to send initial message: ");
            }
        }catch (MessageSendingException e){
            // Просто пробрасываем выше исключение, которое пришло из TelegramHttpService
            // Оно уже содержит все детали (код 403, описание Forbidden)
            throw e;
        }catch (Exception e) {
            // Оборачиваем любые другие непредвиденные ошибки в MessageSendingException
            throw new MessageSendingException("An unexpected error occurred: " + e.getMessage(), e);
        }
    }
}
