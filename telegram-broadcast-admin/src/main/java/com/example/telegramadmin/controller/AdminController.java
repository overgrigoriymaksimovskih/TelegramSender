package com.example.telegramadmin.controller;

import com.example.telegramadmin.dto.MessageRequest;
import com.example.telegramadmin.dto.NotificationResultDto;
import com.example.telegramadmin.exceptions.MessageSendingException;
import com.example.telegramadmin.service.BroadcastOrchestrator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    private final BroadcastOrchestrator broadcastOrchestrator;

    @Autowired
    public AdminController(BroadcastOrchestrator broadcastOrchestrator) {
        this.broadcastOrchestrator = broadcastOrchestrator;
    }

    // GET-эндпойнт для отображения формы
    @GetMapping("/admin/broadcast")
    public String showSendForm(@ModelAttribute("successMessage") String successMessage,
                               @ModelAttribute("errorMessage") String errorMessage,
                               org.springframework.ui.Model model) {

        // Добавляем сообщения, если они есть
        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("successMessage", successMessage);
        }
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("errorMessage", errorMessage);
        }

        // Получаем результаты из flash атрибутов
//        List<NotificationResultDto> sendResults = (List<NotificationResultDto>) model.asMap().get("sendResults");
        Object sendResultsObj = model.asMap().get("sendResults");
        if (sendResultsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<NotificationResultDto> sendResults = (List<NotificationResultDto>) sendResultsObj;
            model.addAttribute("sendResults", sendResults);
        }

        // Добавляем пустой объект MessageRequest для формы, если он не был сохранен
        if (!model.containsAttribute("messageRequest")) {
            model.addAttribute("messageRequest", new MessageRequest());
        }
        return "broadcast";
    }


    // POST-эндпойнт для обработки отправки формы
    @PostMapping("/admin/send")
    public String handleSendForm(@Valid @ModelAttribute ("messageRequest") MessageRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Сохраняем объект request и ошибки валидации, чтобы форма не очищалась
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.messageRequest", bindingResult);
            redirectAttributes.addFlashAttribute("messageRequest", request);
            // Добавляем сообщение об ошибке валидации
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка валидации. Пожалуйста, проверьте введенные данные.");
            return "redirect:/admin/broadcast";
        }

        try {
            // Пытаемся отправить сообщение
            List<NotificationResultDto> sendResults = broadcastOrchestrator.sendMessage(request);
            // Добавляем результаты в атрибуты для отображения на странице
            redirectAttributes.addFlashAttribute("sendResults", sendResults);
            redirectAttributes.addFlashAttribute("successMessage", "Сообщение отправлено! Результаты ниже.");

            return "redirect:/admin/broadcast";
        } catch (MessageSendingException e) {
            // Ловим наше исключение и возвращаем на форму с информацией об ошибке
            // Добавляем атрибут для отображения ошибки на форме
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось отправить сообщение: " + e.getMessage());
            return "redirect:/admin/broadcast";
        } catch (Exception e) { // Ловим другие возможные неожиданные ошибки
            redirectAttributes.addFlashAttribute("errorMessage", "Произошла непредвиденная ошибка: " + e.getMessage());
            return "redirect:/admin/broadcast";
        }
    }
}
