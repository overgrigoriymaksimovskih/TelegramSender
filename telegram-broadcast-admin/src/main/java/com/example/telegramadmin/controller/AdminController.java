package com.example.telegramadmin.controller;

import com.example.telegramadmin.service.BroadcastOrchestrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile; // Импортируем MultipartFile

@Controller
public class AdminController {

//    private final TelegramHttpService telegramHttpService;
    private final BroadcastOrchestrator broadcastOrchestrator;

    @Autowired
    public AdminController(BroadcastOrchestrator broadcastOrchestrator) {
        this.broadcastOrchestrator = broadcastOrchestrator;
    }

    // GET-эндпойнт для отображения формы
    @GetMapping("/admin/broadcast") // Можно использовать любое имя, например /admin/form
    public String showSendForm() {
        return "broadcast"; // Название вашего HTML-шаблона (без .html)
    }

    // POST-эндпойнт для обработки отправки формы
    @PostMapping("/admin/send")
    public String handleSendForm(
            @RequestParam(value = "text", required = false) String text,
            @RequestParam(value = "photo", required = false) MultipartFile photoFile) {

        if ((text == null || text.trim().isEmpty()) && (photoFile == null || photoFile.isEmpty())) {
            // Если ничего не отправлено, можно перенаправить обратно или показать сообщение об ошибке
            System.err.println("Нет данных для отправки.");
            // return "redirect:/admin/send-form?error=nodata"; // Пример редиректа с параметром
            return "redirect:/admin/broadcast"; // Просто редирект
        }

//        if (photoFile != null && !photoFile.isEmpty()) {
//            telegramHttpService.sendPhotoMessage(text, photoFile);
//        } else if (text != null && !text.trim().isEmpty()) {
//            telegramHttpService.sendTextMessage(text);
//        }
        if (photoFile != null && !photoFile.isEmpty()) {
            broadcastOrchestrator.sendMessage(text, photoFile);
        } else if (text != null && !text.trim().isEmpty()) {
            broadcastOrchestrator.sendMessage(text);
        }


        // После отправки можно перенаправить пользователя куда-либо, например, обратно на ту же страницу или на страницу успеха
        return "redirect:/admin/broadcast?success=true"; // Пример редиректа с параметром успеха
    }
}
