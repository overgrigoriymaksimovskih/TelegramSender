package com.example.telegramadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class TgPushService {

    @Value("${bot.token}")
    private String botToken;

    private final RestTemplate rest = new RestTemplate();   // или инжект бина

    public void sendText(Long chatId, String text) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        Map<String, Object> body = Map.of(
                "chat_id", chatId,
                "text",    text,
                "disable_notification", false
        );

        try {
            Map res = rest.postForObject(url, body, Map.class);
            System.out.println("Telegram answer: "+ res);
        } catch (Exception ex) {
            System.out.println("Send failed for chatId= " + chatId + " " +  ex);
        }
    }
}
