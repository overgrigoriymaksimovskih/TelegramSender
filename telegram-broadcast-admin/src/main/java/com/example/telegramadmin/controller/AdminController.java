package com.example.telegramadmin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String showAdminPanel(Model model) {
        // Здесь можно добавить данные для передачи в шаблон
        return "broadcast";
    }
}
