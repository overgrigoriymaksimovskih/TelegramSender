//package com.example.telegramadmin.service;
//
//import org.springframework.stereotype.Service;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import com.example.telegramadmin.dto.NotificationRecipientDto;
//import com.example.telegramadmin.dto.NotificationResultDto;
//import com.example.telegramadmin.enums.NotificationStatus;
//import com.example.telegramadmin.exceptions.MessageSendingException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class MessageCopierService {
//    @Value("${bot.token}")
//    private String botToken;
//
//    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
//    private static final String COPY_MESSAGE_METHOD = "/copyMessage";
//
//    private final RestTemplate restTemplate;
//
//    @Autowired
//    public MessageCopierService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    // Вызывает метод копирования сообщения в АПИ телеграма для каждого получателя (элемента списка)
//    public List<NotificationResultDto> copyMessage(List<NotificationRecipientDto> recipients, Long from_chat_id, Integer message_id) throws MessageSendingException {
//        if (recipients == null || recipients.isEmpty()) {
//            throw new MessageSendingException("Recipients list is empty");
//        }
//
//        if (from_chat_id == null || message_id == null) {
//            throw new MessageSendingException("Source chat ID and message ID are required");
//        }
//
//        String url = TELEGRAM_API_URL + botToken + "/copyMessage";
//        List<NotificationResultDto> results = new ArrayList<>();
//
//        for (NotificationRecipientDto recipient : recipients) {
//            Map<String, Object> params = new HashMap<>();
//            params.put("chat_id", recipient.getTelegramUserId());
//            params.put("from_chat_id", from_chat_id);
//            params.put("message_id", message_id);
//
//            try {
//                ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);
//
//                // Этот код выполнится только при успешном ответе (2xx)
//                results.add(new NotificationResultDto(recipient, NotificationStatus.SUCCESS));
//
//            } catch (HttpClientErrorException e) {
//                // Специфичная обработка HTTP ошибок
//                NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
//
//                if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
//                    failedResult.setDetailedMessage("Bot was blocked by the user or doesn't have access");
//                } else {
//                    failedResult.setDetailedMessage("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
//                }
//                results.add(failedResult);
//
//            } catch (Exception e) {
//                // Обработка всех других исключений (сеть, таймауты и т.д.)
//                NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
//                failedResult.setDetailedMessage("Network error: " + e.getMessage());
//                results.add(failedResult);
//            }
//        }
//        return results;
//    }
//}
//
package com.example.telegramadmin.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.enums.NotificationStatus;
import com.example.telegramadmin.exceptions.MessageSendingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageCopierService {
//    @Value("${bot.token}")
//    private String botToken;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String COPY_MESSAGE_METHOD = "/copyMessage";

    private final WebClient webClient;

    @Autowired
    public MessageCopierService(WebClient webClient) {
        this.webClient = webClient;
    }

    // Вызывает метод копирования сообщения в АПИ телеграма для каждого получателя (элемента списка)
    public List<NotificationResultDto> copyMessage(List<NotificationRecipientDto> recipients, Long from_chat_id, Integer message_id) throws MessageSendingException {
        System.out.println("Мы в функции копирования");
        if (recipients == null || recipients.isEmpty()) {
            throw new MessageSendingException("Recipients list is empty");
        }

        if (from_chat_id == null || message_id == null) {
            throw new MessageSendingException("Source chat ID and message ID are required");
        }

        List<NotificationResultDto> results = new ArrayList<>();

        //Дальше не знаю что делать
        for (NotificationRecipientDto recipient : recipients) {
            System.out.println("Мы в цикле копирования");
            try {
                // Отправляем запрос на копирование сообщения
                String response = webClient.post()
                        .uri(COPY_MESSAGE_METHOD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(Map.of(
                                "chat_id", recipient.getTelegramUserId().toString(),
                                "from_chat_id", from_chat_id,
                                "message_id", message_id
                        ))
                        .retrieve()
                        .bodyToMono(String.class) // ✅ Получаем сырой JSON-ответ как строку
                        .block(); // Блокируем для синхронного сервиса
                System.out.println(response);

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