//package com.example.telegramadmin.service;
//
//import com.example.telegramadmin.dto.CopiedMessage;
//import com.example.telegramadmin.dto.TelegramApiResponse;
//import com.example.telegramadmin.dto.tg_result.Result;
//import org.springframework.http.MediaType;
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
//import org.springframework.web.reactive.function.client.WebClient;
//
//import javax.management.remote.NotificationResult;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class MessageCopierService {
////    @Value("${bot.token}")
////    private String botToken;
//
//    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
//    private static final String COPY_MESSAGE_METHOD = "/copyMessage";
//
//    private final WebClient webClient;
//
//    @Autowired
//    public MessageCopierService(WebClient webClient) {
//        this.webClient = webClient;
//    }
//
//    // Вызывает метод копирования сообщения в АПИ телеграма для каждого получателя (элемента списка)
//    public List<NotificationResultDto> copyMessage(List<NotificationRecipientDto> recipients, Long from_chat_id, Integer message_id) throws MessageSendingException {
//        System.out.println("Мы в функции копирования");
//        if (recipients == null || recipients.isEmpty()) {
//            throw new MessageSendingException("Recipients list is empty");
//        }
//
//        if (from_chat_id == null || message_id == null) {
//            throw new MessageSendingException("Source chat ID and message ID are required");
//        }
//
//        List<NotificationResultDto> results = new ArrayList<>();
//
//        for (NotificationRecipientDto recipient : recipients) {
//            System.out.println("Мы в цикле копирования для пользователя: " + recipient.getTelegramUserId());
//            try {
//                // Отправляем запрос и получаем структурированный ответ
//                TelegramApiResponse apiResponse = webClient.post()
//                        .uri(COPY_MESSAGE_METHOD)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .bodyValue(Map.of(
//                                "chat_id", recipient.getTelegramUserId().toString(),
//                                "from_chat_id", from_chat_id,
//                                "message_id", message_id
//                        ))
//                        .retrieve()
//                        .bodyToMono(TelegramApiResponse.class)
//                        .block();
//
//                // Преобразуем в вашу систему Result
//                Result<CopiedMessage> result = apiResponse.toResult();
//
//                if (result.isSuccess()) {
//                    // Успех - извлекаем ID сообщения
//                    CopiedMessage copiedMessage = ((com.example.telegramadmin.dto.tg_result.Success<CopiedMessage>) result).getValue();
//                    NotificationResultDto successResult = new NotificationResultDto(recipient, NotificationStatus.SUCCESS);
//                    successResult.setDetailedMessage("Message copied successfully. New message ID: " + copiedMessage.getMessageId());
//                    results.add(successResult);
//                } else {
//                    // Обработка ошибки от Telegram API
//                    com.example.telegramadmin.dto.tg_result.Failure<CopiedMessage> failure = (com.example.telegramadmin.dto.tg_result.Failure<CopiedMessage>) result;
//                    NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
//                    failedResult.setDetailedMessage("Telegram API error: " + failure.getDescription());
//                    results.add(failedResult);
//                }
//
//            } catch (HttpClientErrorException e) {
//                // Обработка HTTP ошибок (4xx)
//                NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
//                if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
//                    failedResult.setDetailedMessage("Bot was blocked by the user or doesn't have access");
//                } else {
//                    failedResult.setDetailedMessage("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
//                }
//                results.add(failedResult);
//
//            } catch (Exception e) {
//                // Обработка сетевых ошибок и других исключений
//                NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
//                failedResult.setDetailedMessage("Network error: " + e.getMessage());
//                results.add(failedResult);
//            }
//        }
//        return results;
//    }
//}

package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.CopiedMessage;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.dto.TelegramApiResponse;
import com.example.telegramadmin.dto.tg_result.Result;
import com.example.telegramadmin.enums.NotificationStatus;
import com.example.telegramadmin.exceptions.MessageSendingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class MessageCopierService {

    private static final String COPY_MESSAGE_METHOD = "/copyMessage";
    private static final int MESSAGES_PER_SECOND = 1;
    private static final Duration DELAY_BETWEEN_MESSAGES = Duration.ofMillis(1000 / MESSAGES_PER_SECOND);

    private final WebClient webClient;

    @Autowired
    public MessageCopierService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<NotificationResultDto> copyMessage(List<NotificationRecipientDto> recipients, Long fromChatId, Integer messageId) throws MessageSendingException {
        long startTime = System.currentTimeMillis();

        if (recipients == null || recipients.isEmpty()) {
            throw new MessageSendingException("Список получателей пуст");
        }

        if (fromChatId == null || messageId == null) {
            throw new MessageSendingException("ID исходного чата и ID сообщения обязательны");
        }

        List<NotificationResultDto> results = Flux.fromIterable(recipients)
                .delayElements(DELAY_BETWEEN_MESSAGES) // Задержка между сообщениями
                .flatMap(recipient -> {
                    return processRecipientAsync(recipient, fromChatId, messageId);
                })
                .collectList()
                .block();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Общее время выполнения (мс): " + duration);
        System.out.println("Обработано пользователей: " + (results != null ? results.size() : 0));

        return results;
    }

    private Mono<NotificationResultDto> processRecipientAsync(NotificationRecipientDto recipient, Long fromChatId, Integer messageId) {
        return webClient.post()
                .uri(COPY_MESSAGE_METHOD)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "chat_id", recipient.getTelegramUserId().toString(),
                        "from_chat_id", fromChatId.toString(),
                        "message_id", messageId
                ))
                .retrieve()
                .bodyToMono(TelegramApiResponse.class)
                .map(apiResponse -> {
                    Result<CopiedMessage> result = apiResponse.toResult();

                    if (result.isSuccess()) {
                        CopiedMessage copiedMessage = ((com.example.telegramadmin.dto.tg_result.Success<CopiedMessage>) result).getValue();
                        NotificationResultDto successResult = new NotificationResultDto(recipient, NotificationStatus.SUCCESS);
                        successResult.setDetailedMessage("Сообщение успешно скопировано. Новый ID сообщения: " + copiedMessage.getMessageId());
                        return successResult;
                    } else {
                        com.example.telegramadmin.dto.tg_result.Failure<CopiedMessage> failure = (com.example.telegramadmin.dto.tg_result.Failure<CopiedMessage>) result;
                        NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
                        failedResult.setDetailedMessage("Ошибка Telegram API: " + failure.getDescription());
                        return failedResult;
                    }
                })
                .onErrorResume(e -> {
                    NotificationResultDto errorResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);

                    if (e instanceof WebClientResponseException) {
                        WebClientResponseException webError = (WebClientResponseException) e;

                        if (webError.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                            // Обработка 429 ошибки
                            String retryAfter = webError.getHeaders().getFirst("Retry-After");
                            errorResult.setDetailedMessage("Превышен лимит запросов. Повторите через: " + retryAfter + " секунд");
                            System.out.println("429 ошибка для пользователя " + recipient.getTelegramUserId() +
                                    ", retry-after: " + retryAfter);
                        } else if (webError.getStatusCode() == HttpStatus.FORBIDDEN) {
                            errorResult.setDetailedMessage("Бот заблокирован пользователем или не имеет доступа");
                        } else {
                            errorResult.setDetailedMessage("HTTP ошибка: " + webError.getStatusCode() + " - " + webError.getResponseBodyAsString());
                        }
                    } else {
                        errorResult.setDetailedMessage("Сетевая ошибка: " + e.getMessage());
                    }

                    return Mono.just(errorResult);
                });
    }
}