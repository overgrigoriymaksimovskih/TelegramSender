package com.example.telegramadmin;

import com.example.telegramadmin.service.TelegramHttpService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TelegramAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramAdminApplication.class, args);
    }

    @Bean
    public CommandLineRunner runAfterStart(TelegramHttpService httpService) {
        return args -> {
            httpService.sendStartupMessage();
        };
    }
}