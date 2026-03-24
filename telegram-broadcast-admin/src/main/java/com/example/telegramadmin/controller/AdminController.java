package com.example.telegramadmin.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.telegramadmin.dto.MessageRequest;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.exceptions.MessageSendingException;
import com.example.telegramadmin.service.BroadcastOrchestrator;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    private final BroadcastOrchestrator broadcastOrchestrator;

    @Autowired
    public AdminController(BroadcastOrchestrator broadcastOrchestrator) {
        this.broadcastOrchestrator = broadcastOrchestrator;
    }

    /**
     * GET-эндпойнт для отображения формы отправки сообщений.
     * При первом заходе пользователя на страницу (прямой GET) - эти параметры будут null/пустыми.
     * Пользователь отправит post запрос на /admin/send который вернет его сюда же но с уже заполненными flash-атрибутами
     * Затем этот метод их обработает и вернет соответствующий вид страницы broadcast.html
     */
    @GetMapping("/admin/broadcast") // Этот метод обрабатывает ТОЛЬКО GET /admin/broadcast
    public String showSendForm(@ModelAttribute("successMessage") String successMessage, // Извлекает flash-атрибут successMessage из модели при редиректе (Редирект идет с POST /admin/send на GET /admin/broadcast)
                               @ModelAttribute("errorMessage") String errorMessage, // Извлекает flash-атрибут errorMessage из модели (при редиректе с get /admin/send)

                               // Объект модели Spring MVC для получения данных переданных в редиректе с /admin/send и
                               // Этот же объект модели Spring MVC для передачи данных в представление (HTML-шаблон)
                               // перед отправкой его в хтмл шаблон, на всякий случай проверим что в нем лежит лист с NotificationResultDto
                               org.springframework.ui.Model model) {

        // "successMessage" и "errorMessage" строки, поэтому спринг гарантирует их приведение к строкам и мы можем просто
        // взять их из сигнатуры метода когда это потребуется.
        // "sendResults" же Спринг хранит как Object, и нужно явное приведение типа, поэтому его нужно будет извлеч из
        // "model" самостоятельно... Это не очень логично, потому что мы не видим его здесь но по другому никак...
        // берем его из модели (с "successMessage" и "errorMessage" можно так же, но это лишнее они есть в сигнатуре)

        // этот блок кода нужен только, чтобы убедиться в том, что в model лежит список с объектами ожидаемого типа
        // БЕЗОПАСНОЕ извлечение результатов отправки из flash-атрибутов
        Object sendResultsObj = model.asMap().get("sendResults");
        if (sendResultsObj instanceof List) {
            try {
                // Проверяем, что все элементы списка имеют ожидаемый тип
                List<?> rawList = (List<?>) sendResultsObj;
                if (!rawList.isEmpty() && rawList.get(0) instanceof NotificationResultDto) {
                    // Просто пропускаем это список и в нем объекты ожидаемого типа и он уже в model;
                } else {
                    // Отображаем предупреждение, если тип элементов не совпадает
                    model.addAttribute("sendResults", createErrorResult("Предупреждение: Неожиданный тип элементов в sendResults"));

                }
            } catch (ClassCastException e) {
                // Отображаем ошибку приведения типа, но не прерываем работу
                model.addAttribute("sendResults", createErrorResult("Ошибка приведения типа для sendResults: " + e.getMessage()));
            }
        }
        // Если мы здесь значит в списке лежат объекты типа NotificationResultDto и они корректно отобразятся во view

        // Обеспечиваем наличие объекта MessageRequest для формы
        // Если форма была отправлена с ошибками, сохраняем введенные данные
        if (!model.containsAttribute("messageRequest")) {
            model.addAttribute("messageRequest", new MessageRequest());
        }

        return "broadcast";
    }

    /**
     * POST-эндпойнт для обработки отправки формы
     * Использует паттерн Post-Redirect-Get для избежания повторной отправки формы
     * (Сработает сам когда браузер отправит на /admin/send пост запрос
     * запрос собирается и отправляется в ХТМЛ шаблоне broadcast.html)
     */
    @PostMapping("/admin/send")
    // Здесь Нотификациями мы говорим: полученный запрос нужно смаппить на объект типа messageRequest и валидировать
    // Метод валидации можно найти в самом классе "MesssageRequest" с аннотацией @AssertTrue (true - валидация пройдена)
    // BindingResult сохранит в себя результат маппинга полученного запроса на объект типа Messagerequest
    public String handleSendForm(@Valid @ModelAttribute("messageRequest") MessageRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        // Валидация входных данных формы
        if (bindingResult.hasErrors()) {
            // Сохраняем объект request и ошибки валидации для повторного отображения формы
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.messageRequest", bindingResult);
            redirectAttributes.addFlashAttribute("messageRequest", request);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка валидации. Пожалуйста, проверьте введенные данные.");
            return "redirect:/admin/broadcast";
        }

        try {
            // Отправка сообщения и получение результатов
            List<NotificationResultDto> sendResults = broadcastOrchestrator.sendMessage(request);

            // Сохраняем результаты и сообщение об успехе во flash-атрибуты
            redirectAttributes.addFlashAttribute("sendResults", sendResults);
            redirectAttributes.addFlashAttribute("successMessage", "Сообщение отправлено! Результаты ниже.");

            return "redirect:/admin/broadcast";

        } catch (MessageSendingException e) {
            // Обработка ожидаемых ошибок отправки сообщения
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось отправить сообщение: " + e.getMessage());
            // Сохраняем введенные данные для повторного заполнения формы
            redirectAttributes.addFlashAttribute("messageRequest", request);
            return "redirect:/admin/broadcast";

        } catch (Exception e) {
            // Обработка непредвиденных ошибок
            // Логируем полную информацию об ошибке для debugging
            System.out.println("Непредвиденная ошибка при отправке: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addFlashAttribute("errorMessage", "Произошла непредвиденная ошибка. Пожалуйста, попробуйте еще раз.");
            // Сохраняем введенные данные для повторного заполнения формы
            redirectAttributes.addFlashAttribute("messageRequest", request);
            return "redirect:/admin/broadcast";
        }
    }

    // Вспомогательный метод для создания списка с элементом содержащим только сообщение об ошибке
    private List<NotificationResultDto> createErrorResult(String message) {
        NotificationResultDto errorResult = new NotificationResultDto(null, null);
        errorResult.setDetailedMessage(message);
        return List.of(errorResult);
    }
}
