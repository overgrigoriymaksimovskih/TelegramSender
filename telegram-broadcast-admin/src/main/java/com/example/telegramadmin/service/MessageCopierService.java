package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.enums.NotificationStatus;
import com.example.telegramadmin.exceptions.MessageSendingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageCopierService {
    @Value("${bot.token}")
    private String botToken;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String COPY_MESSAGE_METHOD = "/copyMessage";

    private final RestTemplate restTemplate;

    @Autowired
    public MessageCopierService() {
        this.restTemplate = new RestTemplate();
    }

    // Вызывает метод копирования сообщения в АПИ телеграма для каждого получателя (элемента списка)
    public List<NotificationResultDto> copyMessage(List<NotificationRecipientDto> recipients, Long from_chat_id, Integer message_id) throws MessageSendingException {
        if (recipients == null || recipients.isEmpty()) {
            throw new MessageSendingException("Recipients list is empty");
        }

        if (from_chat_id == null || message_id == null) {
            throw new MessageSendingException("Source chat ID and message ID are required");
        }

        String url = TELEGRAM_API_URL + botToken + "/copyMessage";
        List<NotificationResultDto> results = new ArrayList<>();

        for (NotificationRecipientDto recipient : recipients) {
            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", recipient.getTelegramUserId());
            params.put("from_chat_id", from_chat_id);
            params.put("message_id", message_id);

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);

                // Этот код выполнится только при успешном ответе (2xx)
                results.add(new NotificationResultDto(recipient, NotificationStatus.SUCCESS));

            } catch (HttpClientErrorException e) {
                // Специфичная обработка HTTP ошибок
                NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);

                if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    failedResult.setDetailedMessage("Bot was blocked by the user or doesn't have access");
                } else {
                    failedResult.setDetailedMessage("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                }
                results.add(failedResult);

            } catch (Exception e) {
                // Обработка всех других исключений (сеть, таймауты и т.д.)
                NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
                failedResult.setDetailedMessage("Network error: " + e.getMessage());
                results.add(failedResult);
            }
        }

        return results;
    }
}

