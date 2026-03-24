package com.example.telegramadmin.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.example.telegramadmin.dto.MessageRequest;
import com.example.telegramadmin.exceptions.MessageSendingException;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageSenderService {

    @Value("${bot.token}")
    private String botToken;

    @Value("${target.chatId}")
    private Long targetChatId;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String SEND_MESSAGE_METHOD = "/sendMessage";
    private static final String SEND_PHOTO_METHOD = "/sendPhoto";
    private final RestTemplate restTemplate;

    @Autowired
    public MessageSenderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Определяет какой метод отправки вызывать
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
        System.out.println("Мы в методе отправки первого тестового сообщения через рестТемплейт");
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

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.IMAGE_JPEG);
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
