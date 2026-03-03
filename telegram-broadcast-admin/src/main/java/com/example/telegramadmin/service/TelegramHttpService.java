package com.example.telegramadmin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramHttpService {

    @Value("${bot.token}")
    private String botToken;

    @Value("${target.chatId}")
    private Long targetChatId;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private static final String SEND_MESSAGE_METHOD = "/sendMessage";
    private static final String SEND_PHOTO_METHOD = "/sendPhoto";

    private final RestTemplate restTemplate;

    public TelegramHttpService() {
        this.restTemplate = new RestTemplate();
        // Можно настроить таймауты и другие параметры
        // restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    /**
     * Отправка текстового сообщения в Telegram API
     * @param text Текст сообщения
     * @return Сырой JSON ответ от Telegram API или строка с ошибкой
     */
    public String sendTextMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return createLocalErrorResponse("HttpService <-- Текст сообщения пуст.");
        }
        return sendMessage(text, null);
    }

    /**
     * Отправка сообщения с изображением в Telegram API
     * @param caption Подпись к изображению (может быть null)
     * @param photoFile Файл изображения
     * @return Сырой JSON ответ от Telegram API или строка с ошибкой
     */
    public String sendPhotoMessage(String caption, MultipartFile photoFile) {
        if (photoFile == null || photoFile.isEmpty()) {
            return createLocalErrorResponse("HttpService <-- Файл изображения не выбран или пуст.");
        }
        return sendMessage(caption, photoFile);
    }

    /**
     * Основной метод отправки сообщения
     * @param text Текст сообщения или подпись
     * @param photoFile Файл изображения (null для текстового сообщения)
     * @return Сырой JSON ответ от Telegram API или строка с ошибкой
     */
    private String sendMessage(String text, MultipartFile photoFile) {
        // Валидация конфигурации
        if (botToken == null || botToken.isEmpty()) {
            return createLocalErrorResponse("HttpService <-- bot.token не задан.");
        }

        if (targetChatId == null) {
            return createLocalErrorResponse("HttpService <-- target.chatId не задан.");
        }

        try {
            if (photoFile == null) {
                // Текстовое сообщение
                return sendTextMessageToApi(text);
            } else {
                // Сообщение с фото
                return sendPhotoMessageToApi(text, photoFile);
            }
        } catch (Exception e) {
            // Логируем исключение и возвращаем ошибку в JSON формате
            return createExceptionResponse(e);
        }
    }

    /**
     * Отправка текстового сообщения через Telegram API
     */
    private String sendTextMessageToApi(String text) {
        String url = TELEGRAM_API_URL + botToken + SEND_MESSAGE_METHOD;

        // Подготовка параметров
        Map<String, Object> params = new HashMap<>();
        params.put("chat_id", targetChatId);
        params.put("text", text);
        params.put("parse_mode", "HTML"); // Можно сделать конфигурируемым

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    params,
                    String.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            // 4xx ошибки (клиентские)
            return createHttpErrorResponse(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            // 5xx ошибки (серверные)
            return createHttpErrorResponse(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            // Остальные ошибки RestTemplate
            return createRestClientExceptionResponse(e);
        }
    }

    /**
     * Отправка сообщения с фото через Telegram API
     */
    private String sendPhotoMessageToApi(String caption, MultipartFile photoFile) {
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
            if (fileBytes.length == 0) {
                return createLocalErrorResponse("Не удалось прочитать файл изображения.");
            }

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

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // Отправка запроса
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            return createHttpErrorResponse(e.getStatusCode(), e.getResponseBodyAsString());
        } catch (RestClientException e) {
            return createRestClientExceptionResponse(e);
        } catch (IOException e) {
            return createLocalErrorResponse("Ошибка чтения файла: " + e.getMessage());
        }
    }

    /**
     * Создание ответа для локальных ошибок (до вызова API)
     */
    private String createLocalErrorResponse(String errorMessage) {
        // Форматируем как JSON, чтобы фабрика могла парсить
        return String.format("""
            {
                "ok": false,
                "error_code": 400,
                "description": "%s",
                "local_error": true
            }
            """, errorMessage.replace("\"", "\\\""));
    }

    /**
     * Создание ответа для HTTP ошибок
     */
    private String createHttpErrorResponse(HttpStatusCode statusCode, String responseBody) {
        // Если Telegram уже вернул JSON с ошибкой, возвращаем его как есть
        if (responseBody != null && responseBody.trim().startsWith("{")) {
            return responseBody;
        }

        // Иначе создаем свой JSON
        return String.format("""
            {
                "ok": false,
                "error_code": %d,
                "description": "HTTP Error: %s",
                "http_status": %d
            }
            """,
                statusCode.value(),
                statusCode.toString(),
                statusCode.value()
        );
    }

    /**
     * Создание ответа для исключений RestClient
     */
    private String createRestClientExceptionResponse(RestClientException e) {
        return String.format("""
            {
                "ok": false,
                "error_code": 500,
                "description": "RestClient Error: %s",
                "exception_type": "%s"
            }
            """,
                e.getMessage(),
                e.getClass().getSimpleName()
        );
    }

    /**
     * Создание ответа для общих исключений
     */
    private String createExceptionResponse(Exception e) {
        return String.format("""
            {
                "ok": false,
                "error_code": 500,
                "description": "Exception: %s",
                "exception_type": "%s"
            }
            """,
                e.getMessage(),
                e.getClass().getSimpleName()
        );
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

//    /**
//     * Дополнительные методы для других операций Telegram API
//     */
//
//    public String getUpdates(Long offset) {
//        String url = TELEGRAM_API_URL + botToken + "/getUpdates";
//
//        Map<String, Object> params = new HashMap<>();
//        if (offset != null) {
//            params.put("offset", offset);
//        }
//        params.put("timeout", 30); // Можно вынести в конфиг
//
//        try {
//            ResponseEntity<String> response = restTemplate.postForEntity(
//                    url,
//                    params,
//                    String.class
//            );
//            return response.getBody();
//        } catch (Exception e) {
//            return createExceptionResponse(e);
//        }
//    }
//
//    public String getMe() {
//        String url = TELEGRAM_API_URL + botToken + "/getMe";
//
//        try {
//            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
//            return response.getBody();
//        } catch (Exception e) {
//            return createExceptionResponse(e);
//        }
//    }
//
//    /**
//     * Метод для проверки соединения с Telegram API
//     */
//    public String testConnection() {
//        return getMe();
//    }
}
