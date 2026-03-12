package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.enums.NotificationStatus;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.ResourceAccessException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.example.telegramadmin.exceptions.MessageSendingException;
import com.example.telegramadmin.dto.MessageRequest;

@Service
public class TelegramHttpService {

    @Value("${bot.token}")
    private String botToken;

    @Value("${target.chatId}")
    private Long targetChatId;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String SEND_MESSAGE_METHOD = "/sendMessage";
    private static final String SEND_PHOTO_METHOD = "/sendPhoto";
    private static final String COPY_MESSAGE_METHOD = "/copyMessage";


    private final RestTemplate restTemplate;

    public TelegramHttpService() {
        this.restTemplate = new RestTemplate();
        // Можно настроить таймауты и другие параметры
        // restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }
//    public void copyMessage(List<NotificationRecipientDto> recipients, Long from_chat_id, Integer message_id) throws MessageSendingException {
//        if (recipients == null || recipients.isEmpty()) {
//            System.out.println("Recipients list is empty");
//            throw new MessageSendingException("Recipients list is empty");
//        }
//
//        if (from_chat_id == null || message_id == null) {
//            System.out.println("Source chat ID and message ID are required");
//            throw new MessageSendingException("Source chat ID and message ID are required");
//        }
//
//        String url = TELEGRAM_API_URL + botToken + "/copyMessage";
//
//        for (NotificationRecipientDto recipient : recipients) {
//            // Подготовка параметров для каждого получателя
//            Map<String, Object> params = new HashMap<>();
//            params.put("chat_id", recipient.getTelegramUserId()); // ID получателя
//            params.put("from_chat_id", from_chat_id);            // ID чата-источника
//            params.put("message_id", message_id);                // ID сообщения для копирования
//
//            try {
//                ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);
//
//                if (!response.getStatusCode().is2xxSuccessful()) {
//                    // Логируем ошибку, но продолжаем отправку другим получателям
//                    System.err.println("Failed to copy message to user " + recipient.getTelegramUserId() +
//                            ": " + response.getStatusCode());
//                }else{
//                    System.out.println("Not failed to copy message " + message_id + " to user " + recipient.getTelegramUserId() + " from " + from_chat_id +
//                            ": " + response.getStatusCode());
//                }
//
//            } catch (Exception e) {
//                // Логируем ошибку, но продолжаем отправку другим получателям
//                System.err.println("Error copying message to user " + recipient.getTelegramUserId() +
//                        ": " + e.getMessage());
//            }
//        }
//    }

    public List<NotificationResultDto> copyMessage(List<NotificationRecipientDto> recipients, Long from_chat_id, Integer message_id) throws MessageSendingException {
        if (recipients == null || recipients.isEmpty()) {
            System.out.println("Recipients list is empty");
            throw new MessageSendingException("Recipients list is empty");
        }

        if (from_chat_id == null || message_id == null) {
            System.out.println("Source chat ID and message ID are required");
            throw new MessageSendingException("Source chat ID and message ID are required");
        }

        String url = TELEGRAM_API_URL + botToken + "/copyMessage";
        List<NotificationResultDto> results = new ArrayList<>();

        for (NotificationRecipientDto recipient : recipients) {
            // Подготовка параметров для каждого получателя
            Map<String, Object> params = new HashMap<>();
            params.put("chat_id", recipient.getTelegramUserId()); // ID получателя
            params.put("from_chat_id", from_chat_id);            // ID чата-источника
            params.put("message_id", message_id);                // ID сообщения для копирования

            try {
                ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    // Успешная отправка
                    results.add(new NotificationResultDto(recipient, NotificationStatus.SUCCESS));
                    System.out.println("Successfully copied message " + message_id + " to user " + recipient.getTelegramUserId());
                } else {
                    // Ошибка HTTP
                    NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
                    failedResult.setDetailedMessage("HTTP Error: " + response.getStatusCode());
                    results.add(failedResult);

                    System.err.println("Failed to copy message to user " + recipient.getTelegramUserId() +
                            ": " + response.getStatusCode());
                }

            } catch (Exception e) {
                // Ошибка сети или другая исключительная ситуация
                NotificationResultDto failedResult = new NotificationResultDto(recipient, NotificationStatus.ERROR);
                failedResult.setDetailedMessage("Exception: " + e.getMessage());
                results.add(failedResult);

                System.err.println("Error copying message to user " + recipient.getTelegramUserId() +
                        ": " + e.getMessage());
            }
        }

        return results;
    }



    public String sendMessage(MessageRequest request) throws MessageSendingException {
        String text = request.getText();
        MultipartFile photoFile = request.getPhoto();

        // Валидация конфигурации
        if (botToken == null || botToken.isEmpty()) {
            throw new MessageSendingException("Bot token is not configured (bot.token).");
        }
        if (targetChatId == null) {
            throw new MessageSendingException("Target chat ID is not configured (target.chatId).");
        }

        // Определяем какой метод использовать на основе наличия файла
        if (photoFile == null || photoFile.isEmpty()) {
            return sendTextMessageToApi(text);
        } else {
            // Проверяем что файл действительно содержит данные
            if (photoFile.getSize() == 0) {
                throw new MessageSendingException("Photo file is empty");
            }
            return sendPhotoMessageToApi(text, photoFile);
        }
}

    // Отправка текстового сообщения через Telegram API
    private String sendTextMessageToApi(String text)  throws MessageSendingException {
        String url = TELEGRAM_API_URL + botToken + SEND_MESSAGE_METHOD;

        // Подготовка параметров
        Map<String, Object> params = new HashMap<>();
        params.put("chat_id", targetChatId);
        params.put("text", text);
        params.put("parse_mode", "HTML"); // Можно сделать конфигурируемым

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, params, String.class);
            // Проверяем статус ответа. Если не 2xx, бросаем исключение
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MessageSendingException("Telegram API returned non-success status: " + response.getStatusCode());
            }
            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Это ошибки 4xx и 5xx от Telegram
            String errorDetail = e.getResponseBodyAsString();
            throw new MessageSendingException("Telegram API error: " + e.getStatusCode() + " - " + errorDetail, e);
        } catch (ResourceAccessException e) {
            // Проблемы с сетью (таймаут, нет соединения)
            throw new MessageSendingException("Network error while calling Telegram API: " + e.getMessage(), e);
        } catch (RestClientException e) {
            // Все остальные ошибки RestClient
            throw new MessageSendingException("RestClient error: " + e.getMessage(), e);
        }
    }

    // Отправка сообщения с фото через Telegram API
    private String sendPhotoMessageToApi(String caption, MultipartFile photoFile) throws MessageSendingException {
        // Проверяем что файл не пустой
        if (photoFile == null || photoFile.isEmpty() || photoFile.getSize() == 0) {
            throw new MessageSendingException("Photo file is required for photo message");
        }

        String url = TELEGRAM_API_URL + botToken + SEND_PHOTO_METHOD;

        try {
            // Подготовка multipart запроса
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // Часть с chat_id
            body.add("chat_id", targetChatId.toString());

            // Часть с caption (если есть)
            if (caption != null && !caption.trim().isEmpty()) {
                body.add("caption", caption);
                body.add("parse_mode", "HTML");
            }

            // Часть с файлом
            byte[] fileBytes = getFileBytes(photoFile);
//            if (fileBytes.length == 0) {
////                return createLocalErrorResponse("Не удалось прочитать файл изображения.");
//            }

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.IMAGE_JPEG); // или определять по расширению
            fileHeaders.setContentDisposition(
                    ContentDisposition.builder("form-data")
                            .name("photo")
                            .filename(photoFile.getOriginalFilename())
                            .build()
            );

            body.add("photo", new HttpEntity<>(fileBytes, fileHeaders));

            // Подготовка заголовков запроса
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MessageSendingException("Telegram API returned non-success status: " + response.getStatusCode());
            }
            return response.getBody();

        } catch (IOException e) {
            throw new MessageSendingException("Failed to read photo file: " + e.getMessage(), e);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String errorDetail = e.getResponseBodyAsString();
            throw new MessageSendingException("Telegram API error: " + e.getStatusCode() + " - " + errorDetail, e);
        } catch (ResourceAccessException e) {
            throw new MessageSendingException("Network error while calling Telegram API: " + e.getMessage(), e);
        } catch (RestClientException e) {
            throw new MessageSendingException("RestClient error: " + e.getMessage(), e);
        }
    }


    /**
     * Вспомогательный метод для получения байтов из MultipartFile
     */
    private byte[] getFileBytes(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return new byte[0];
        }
        return file.getBytes();
    }
}
