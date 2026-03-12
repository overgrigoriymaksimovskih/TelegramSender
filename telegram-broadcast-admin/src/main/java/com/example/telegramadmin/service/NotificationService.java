package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.MessageRequest;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.dto.tg_result.Result;
import com.example.telegramadmin.dto.tg_result.Success;
import com.example.telegramadmin.enums.NotificationStatus;
import com.example.telegramadmin.exceptions.MessageSendingException;
import com.example.telegramadmin.factory.TelegramResultFactory;
import com.example.telegramadmin.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final TelegramHttpService telegramHttpService;
    private final AppUserRepository appUserRepository;
    private final TelegramResultFactory telegramResultFactory;

    @Autowired
    public NotificationService(AppUserRepository appUserRepository, TelegramHttpService telegramHttpService, TelegramResultFactory telegramResultFactory) {
        this.telegramHttpService = telegramHttpService;
        this.telegramResultFactory = telegramResultFactory;
        this.appUserRepository = appUserRepository;
    }

    public List<NotificationRecipientDto> getRecipientsDtoList(Long telegramUserId) {
        List<NotificationRecipientDto> result = appUserRepository.findNotificationDtoByTelegramUserId(telegramUserId);
        return new ArrayList<>(result);
    }

    public List<NotificationResultDto> getFailedNotifications(List<NotificationResultDto> results) {
        return results.stream()
                .filter(result -> result.getStatus() != NotificationStatus.SUCCESS)
                .collect(Collectors.toList());
    }

    public List<NotificationResultDto> sendCopyOfMessageToRecipients (List<NotificationRecipientDto> notificationRecipientsDtoList, Result<Message> result) throws MessageSendingException {
        // Внутри result хранится объект Message
        Message message = ((Success<Message>) result).getValue();
        return telegramHttpService.copyMessage(notificationRecipientsDtoList, message.getChatId(), message.getMessageId());
    }

    public String crateMainMessage (MessageRequest request) throws MessageSendingException {
        return telegramHttpService.sendMessage(request);
    }
}
