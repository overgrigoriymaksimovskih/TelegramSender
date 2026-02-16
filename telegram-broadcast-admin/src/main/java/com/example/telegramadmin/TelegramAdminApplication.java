package com.example.telegramadmin;

import com.example.telegramadmin.entity.AppUser;
import com.example.telegramadmin.repository.AppUserRepository;
import com.example.telegramadmin.service.TgPushService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TelegramAdminApplication {

    /* ==== Telegram-ID получателя ==== */
    private static final Long TARGET_TG_ID = 6128969029L;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx =
                SpringApplication.run(TelegramAdminApplication.class, args);

        AppUserRepository repo   = ctx.getBean(AppUserRepository.class);
        TgPushService     sender = ctx.getBean(TgPushService.class);

        AppUser u = repo.findByTelegramUserId(TARGET_TG_ID).orElse(null);
        if (u == null) {
            System.out.println("❌ Пользователь не найден");
        } else {
            System.out.println("✅ Отправляем сообщение пользователю " + u.getUsername());
            sender.sendText(u.getTelegramUserId(), "Тестовое сообщение из Spring ☕");
        }

        ctx.close();
    }
}
