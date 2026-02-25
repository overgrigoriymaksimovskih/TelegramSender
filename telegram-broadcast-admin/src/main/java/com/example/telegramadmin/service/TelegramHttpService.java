package com.example.telegramadmin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramHttpService {

    // Токен бота и целевой чат (chat_id)
    @Value("${bot.token}")
    private String botToken;

    @Value("${target.chatId}")
    private Long targetChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendStartupMessage() {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", targetChatId);
        payload.put("text", "Бот запущен и готов к отправке сообщений.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            // можно логировать успех
        } catch (Exception e) {
            // обработка ошибки
            System.err.println("Ошибка отправки startup сообщения: " + e.getMessage());
        }
    }
}