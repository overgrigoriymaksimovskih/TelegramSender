package com.example.telegramadmin.factory;

import com.example.telegramadmin.dto.tg_result.Failure;
import com.example.telegramadmin.dto.tg_result.Result;
import com.example.telegramadmin.dto.tg_result.Success;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

/**
 * Фабрика для создания результатов работы с Telegram API.
 * Метод {@link #fromTelegramResponse(String, Class)} анализирует JSON‑ответ
 * и возвращает {@code Result<T>}.
 */
@Component
public class TelegramResultFactory {

    private final ObjectMapper objectMapper;

    public TelegramResultFactory() {
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // регистрируем JSR‑310 и т.п.
    }

    /**
     * Анализирует JSON‑ответ от Telegram API и создаёт объект {@code Result<T>}.
     *
     * @param json   строка JSON, полученная от Telegram
     * @param clazz  класс, в который нужно превратить поле {@code result}
     * @return {@code Success<T>} при успешном ответе, иначе {@code Failure<T>}
     */
    public <T> Result<T> fromTelegramResponse(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) {
            return createFailure(null, "Пустой ответ от Telegram");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (Exception e) {
            return createFailure(e);
        }

        // 1️⃣ Проверяем наличие поля "ok"
        JsonNode okNode = root.get("ok");
        if (okNode == null || !okNode.isBoolean()) {
            return createFailure(null, "Ответ не содержит булево поле \"ok\"");
        }

        boolean ok = okNode.asBoolean();

        if (ok) {
            // 2️⃣ Успешный ответ – пытаемся извлечь "result"
            JsonNode resultNode = root.get("result");
            if (resultNode == null || resultNode.isNull()) {
                // Telegram иногда возвращает ok:true без result (например, при ping)
                return createFailure(200, "ok:true, но поле \"result\" отсутствует");
            }

            try {
                // Преобразуем result в нужный тип
                T data = objectMapper.treeToValue(resultNode, clazz);
                return createSuccess(data);
            } catch (MismatchedInputException e) {
                // Тип не совпадает (например, result – не Message)
                return createFailure(200, "Неподдерживаемый тип результата: " + clazz.getSimpleName());
            } catch (Exception e) {
                return createFailure(e);
            }
        } else {
            // 3️⃣ Ошибка API – извлекаем код и описание
            JsonNode errorCodeNode = root.get("error_code");
            JsonNode descriptionNode = root.get("description");

            Integer errorCode = errorCodeNode != null && errorCodeNode.isInt()
                    ? errorCodeNode.intValue()
                    : null;
            String description = descriptionNode != null && descriptionNode.isTextual()
                    ? descriptionNode.textValue()
                    : "Неизвестная ошибка";

            return createFailure(errorCode, description);
        }
    }

    /* ------------------------------------------------------------------ */
    /* Вспомогательные методы для создания Success/Failure                */
    /* ------------------------------------------------------------------ */

    public <T> Success<T> createSuccess(T data) {
        return new Success<>(data);
    }

    public <T> Failure<T> createFailure(Integer errorCode, String description) {
        return new Failure<>(errorCode, description);
    }

    public <T> Failure<T> createFailure(Exception exception) {
        return new Failure<>(exception);
    }
}
