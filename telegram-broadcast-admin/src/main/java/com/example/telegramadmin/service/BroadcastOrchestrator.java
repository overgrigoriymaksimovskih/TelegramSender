package com.example.telegramadmin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.telegramadmin.dto.TelegramApiRequest;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.dto.tg_result.Result;
import com.example.telegramadmin.dto.tg_result.Success;
import com.example.telegramadmin.factory.TelegramResultFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import java.util.stream.Collectors;
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
    public List<NotificationResultDto> sendMessage(TelegramApiRequest request)  throws MessageSendingException{
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


//----------------------------------------------------------------------------------------------------------------------
                // Код для тестовой проверки получения списка получателей из базы
                // Получаем список пользователей для уведомлений и печатаем его в консоль
                List<NotificationRecipientDto> notificationRecipientsDtoList = recipientService.getRecipientsDtoList();
                for(NotificationRecipientDto recipient :notificationRecipientsDtoList){
                    System.out.println(recipient.getFirstName() + " " + recipient.getTelegramUserId());
                }
                return null;
//----------------------------------------------------------------------------------------------------------------------


////----------------------------------------------------------------------------------------------------------------------
//                // Код для тестовой отправки собранному вручную списку пользователей
//                List<NotificationRecipientDto> notificationRecipientsDtoList = new ArrayList<>();
//                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L,"testing recipient 1"));
//                notificationRecipientsDtoList.add(new NotificationRecipientDto(61289690299L,"testing recipient 2"));
//                notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L,"testing recipient 3"));
//                for (int i = 4; i <= 10; i++) {
//                    notificationRecipientsDtoList.add(new NotificationRecipientDto(6128969029L,"testing recipient " + i));
//                }
//
//                List<NotificationResultDto> allNotifications = messageCopierService.copyMessage(notificationRecipientsDtoList, message.getChatId(), message.getMessageId());
//
//                for(NotificationResultDto dto:allNotifications){
//                    System.out.println(dto.getStatus() + " -> " + dto.getUser().getTelegramUserId() + " -> " + dto.getUser().getFirstName() + " -> " + dto.getDetailedMessage());
//                }
//                return allNotifications;
////----------------------------------------------------------------------------------------------------------------------


////----------------------------------------------------------------------------------------------------------------------
//                //Код для тестовой отправки полученному из БД и отфильтрованному списку пользователей
//                // !!! Получаем список пользователей для уведомлений !!!
//                List<NotificationRecipientDto> notificationRecipientsDtoList = recipientService.getRecipientsDtoList();
//
//                // Задаем ИД которым отправим сообщение если эти ИД есть в полученном списке
//                List<Long> targetIds = List.of(6128969029L, 61289690299L, 610200129L);
//
//                // Получаем список с выбранными ИД (фильтруем)
//                List<NotificationRecipientDto> filteredRecipients = notificationRecipientsDtoList.stream()
//                        .filter(recipient -> targetIds.contains(recipient.getTelegramUserId()))
//                        .collect(Collectors.toList());
//
//                // Отправляем копию на рассылку
//                List<NotificationResultDto> allNotifications = messageCopierService.copyMessage(filteredRecipients, message.getChatId(), message.getMessageId());
//
//                for(NotificationResultDto dto:allNotifications){
//                    System.out.println(dto.getStatus() + " -> " + dto.getUser().getTelegramUserId() + " -> " + dto.getUser().getFirstName() + " -> " + dto.getDetailedMessage());
//                }
//                return allNotifications;
////----------------------------------------------------------------------------------------------------------------------
















////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//                // КОД ДЛЯ РЕАЛЬНОЙ ОТПРАВКИ КОПИИ СООБЩЕНИЯ ВСЕМ ПОЛЬЗОВАТЕЛЯМ

//                // !!! Получаем список пользователей для уведомлений !!!
//                List<NotificationRecipientDto> notificationRecipientsDtoList = recipientService.getRecipientsDtoList();

//                // !!! Копируем отправленное сообщение всем пользователям !!!
//                List<NotificationResultDto> allNotifications = messageCopierService.copyMessage(notificationRecipientsDtoList, message.getChatId(), message.getMessageId());

//                for(NotificationResultDto dto:allNotifications){
//                    System.out.println(dto.getStatus() + " -> " + dto.getUser().getTelegramUserId() + " -> " + dto.getUser().getFirstName() + " -> " + dto.getDetailedMessage());
//                }
//                return allNotifications;
////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


            } else {
                // Обработка случая, если основное сообщение не отправилось
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
