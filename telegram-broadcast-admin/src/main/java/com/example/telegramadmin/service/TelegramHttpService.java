package com.example.telegramadmin.service;

import com.example.telegramadmin.dto.SentMessageInfo;
import com.example.telegramadmin.dto.TelegramApiResponse;
import com.example.telegramadmin.dto.TelegramMessageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile; // Импортируем MultipartFile

import java.util.HashMap;
import java.util.Map;
import java.io.IOException; // Импортируем IOException

@Service
public class TelegramHttpService {

    @Value("${bot.token}")
    private String botToken;

    @Value("${target.chatId}")
    private Long targetChatId;

    private final RestTemplate restTemplate = new RestTemplate();

    // Уберем поле defaultText, так как оно не будет использоваться для отправки картинок
    // private String defaultText = "Бот запущен и готов к отправке сообщений.";

    // Сеттер для текста из формы (оставим, если нужен для текстовых сообщений)
    // public void setDefaultText(String text) {
    //     if (text != null && !text.trim().isEmpty()) {
    //         this.defaultText = text;
    //     }
    // }

    // Метод для отправки только текстового сообщения
    public TelegramApiResponse sendTextMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            System.err.println("Текст сообщения пуст.");
        }
        return sendMessage(text, null); // Передаем null для файла
    }

    // Метод для отправки сообщения с изображением
    public TelegramApiResponse sendPhotoMessage(String caption, MultipartFile photoFile) {
        if (photoFile == null || photoFile.isEmpty()) {
            System.err.println("Файл изображения не выбран или пуст.");
        }
        return sendMessage(caption, photoFile); // Передаем caption и файл
    }

//    // Основной метод для отправки сообщений (текст или фото)
//    private void sendMessage(String text, MultipartFile photoFile) {
//        if (botToken == null || botToken.isEmpty()) {
//            System.err.println("bot.token не задан.");
//            return;
//        }
//        if (targetChatId == null) {
//            System.err.println("target.chatId не задан.");
//            return;
//        }
//
//        String url;
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<?> requestEntity;
//
//        if (photoFile != null && !photoFile.isEmpty()) {
//            // Отправка фото
//            url = String.format("https://api.telegram.org/bot%s/sendPhoto", botToken);
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            // Создаем MultiValueMap для отправки файла
//            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
//            body.add("chat_id", targetChatId);
//            // Добавляем файл как "content" с именем файла
//            body.add("photo", new ByteArrayResource(getFileBytes(photoFile)) {
//                @Override
//                public String getFilename() {
//                    return photoFile.getOriginalFilename();
//                }
//            });
//            // Добавляем подпись, если она есть
//            if (text != null && !text.trim().isEmpty()) {
//                body.add("caption", text);
//            }
//
//            requestEntity = new HttpEntity<>(body, headers);
//
//        } else if (text != null && !text.trim().isEmpty()) {
//            // Отправка только текстового сообщения
//            url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
//            headers.setContentType(MediaType.APPLICATION_JSON);
//
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("chat_id", targetChatId);
//            payload.put("text", text);
//
//            requestEntity = new HttpEntity<>(payload, headers);
//        } else {
//            System.err.println("Нет ни текста, ни файла для отправки.");
//            return;
//        }
//
//        try {
//            System.out.println("Отправка на URL: " + url);
//            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
//            System.out.println("Ответ Telegram API: " + response.getBody());
//        } catch (Exception e) {
//            System.err.println("Ошибка отправки сообщения/фото: " + e.getMessage());
//            // Логирование более подробной информации об ошибке, если возможно
//            if (e.getCause() != null) {
//                System.err.println("Причина ошибки: " + e.getCause().getMessage());
//            }
//        }
//    }
// Основной метод для отправки сообщений (текст или фото)
// Теперь он возвращает TelegramApiResponse для анализа
private TelegramApiResponse sendMessage(String text, MultipartFile photoFile) {
    if (botToken == null || botToken.isEmpty()) {
        System.err.println("bot.token не задан.");
        // Возвращаем объект, сигнализирующий об ошибке
        return createErrorResponse("bot.token не задан.");
    }
    if (targetChatId == null) {
        System.err.println("target.chatId не задан.");
        return createErrorResponse("target.chatId не задан.");
    }

    String url;
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<?> requestEntity;

    if (photoFile != null && !photoFile.isEmpty() && photoFile.getSize() > 0) { // Добавил проверку photoFile.getSize() > 0
        // Отправка фото
        url = String.format("https://api.telegram.org/bot%s/sendPhoto", botToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", targetChatId);
        body.add("photo", new ByteArrayResource(getFileBytes(photoFile)) {
            @Override
            public String getFilename() {
                return photoFile.getOriginalFilename();
            }
        });
        if (text != null && !text.trim().isEmpty()) {
            body.add("caption", text);
        }
        requestEntity = new HttpEntity<>(body, headers);

    } else if (text != null && !text.trim().isEmpty()) {
        // Отправка только текстового сообщения
        url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", targetChatId);
        payload.put("text", text);
        requestEntity = new HttpEntity<>(payload, headers);
    } else {
        System.err.println("Нет ни текста, ни файла для отправки.");
        return createErrorResponse("Нет ни текста, ни файла для отправки.");
    }

    try {
        System.out.println("Отправка на URL: " + url);
        // !!! ИЗМЕНЕНИЕ: Ожидаем TelegramApiResponse вместо String !!!
        ResponseEntity<TelegramApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                TelegramApiResponse.class // Указываем нашу DTO
        );

        if (response.getBody() != null) {
            System.out.println("Ответ Telegram API получен.");
            TelegramApiResponse apiResponse = response.getBody();

            if (apiResponse.isOk() && apiResponse.getResult() != null) {
                // --- Успешная отправка ---
                TelegramMessageResult messageResult = apiResponse.getResult();
                Long chatId = messageResult.getChat().getId();
                Integer messageId = messageResult.getMessage_id();

                System.out.println("Сообщение успешно отправлено. Chat ID: " + chatId + ", Message ID: " + messageId);

                // !!! Здесь сохраняем информацию для дальнейшей рассылки !!!
                SentMessageInfo sentInfo = new SentMessageInfo(chatId, messageId);
                // savedSentMessageInfo = sentMessageRepository.save(sentInfo); // Пример сохранения в БД

                return apiResponse; // Возвращаем полный ответ для демонстрации
            } else {
                // --- Ошибка от Telegram API ---
                System.err.println("Ошибка Telegram API:");
                // Здесь нужно будет получить описание ошибки.
                // Telegram API может возвращать { "ok": false, "error_code": ..., "description": "..." }
                // Для этого нужно будет расширить TelegramApiResponse или создать отдельный класс для ошибки.
                // Пока что просто выведем, что есть.
                System.err.println("Статус 'ok': " + (apiResponse != null ? apiResponse.isOk() : "null"));
                // Предполагаем, что в случае ошибки 'result' может быть null, а на верхнем уровне есть описание ошибки
                // Для более полного анализа нужно парсить String ответ или создать классы для ошибок Telegram.
                // Для простоты, предположим, что ответ == null или apiResponse.getResult() == null
                if (apiResponse == null) {
                    System.err.println("Тело ответа пустое.");
                } else {
                    System.err.println("Ответ API (неполный): " + apiResponse.toString()); // Нужно добавить toString или парсить raw response
                }
                return apiResponse; // Возвращаем (возможно, некорректный) ответ
            }
        } else {
            System.err.println("Тело ответа от Telegram API пустое.");
            return createErrorResponse("Тело ответа от Telegram API пустое.");
        }

    } catch (Exception e) {
        System.err.println("Ошибка выполнения HTTP-запроса: " + e.getMessage());
        e.printStackTrace(); // Для отладки
        return createErrorResponse("Ошибка выполнения HTTP-запроса: " + e.getMessage());
    }
}

    // Вспомогательный метод для получения байтов из MultipartFile
    private byte[] getFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            System.err.println("Ошибка чтения байтов файла: " + e.getMessage());
            return new byte[0]; // Возвращаем пустой массив, если произошла ошибка
        }
    }

    /**
     * Вспомогательный метод для создания объекта TelegramApiResponse, сигнализирующего об ошибке.
     * Для реальной обработки ошибок Telegram API, вам, вероятно, понадобится парсить String-ответ.
     */
    private TelegramApiResponse createErrorResponse(String errorMessage) {
        TelegramApiResponse errorResp = new TelegramApiResponse();
        errorResp.setOk(false);
        // В случае ошибки, result будет null, и описание ошибки нужно будет получать иначе.
        // Здесь просто возвращаем объект с ok=false, чтобы внешняя логика могла это проверить.
        System.err.println("Создан объект ошибки: " + errorMessage);
        return errorResp;
    }
}
