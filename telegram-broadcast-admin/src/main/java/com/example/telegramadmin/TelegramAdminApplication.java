package com.example.telegramadmin;

import com.example.telegramadmin.entity.AppUser;
import com.example.telegramadmin.repository.AppUserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TelegramAdminApplication {

    /* ==== ХАРДКОД id Telegram-юзера, чьи данные покажем ==== */
    private static final Long TARGET_TG_ID = 610200129L;   // <-- ставь свой

    public static void main(String[] args) {
        /* запускаем Spring и сразу используем контекст */
        ConfigurableApplicationContext ctx =
                SpringApplication.run(TelegramAdminApplication.class, args);

        /* достаём бин репозитория */
        AppUserRepository repo = ctx.getBean(AppUserRepository.class);

        /* ищем юзера в БД */
        AppUser u = repo.findByTelegramUserId(TARGET_TG_ID).orElse(null);

        if (u == null) {
            System.out.println("❌ Юзер с telegram_user_id=" + TARGET_TG_ID + " не найден");
        } else {
            System.out.println("✅ Данные пользователя:");
            System.out.println("id               : " + u.getId());
            System.out.println("telegram_user_id : " + u.getTelegramUserId());
            System.out.println("first_login_date : " + u.getFirstLoginDate());
            System.out.println("first_name       : " + u.getFirstName());
            System.out.println("last_name        : " + u.getLastName());
            System.out.println("username         : " + u.getUsername());
            System.out.println("sms_code         : " + u.getSmsCode());
            System.out.println("email            : " + u.getEmail());
            System.out.println("site_user_id     : " + u.getSiteUserId());
            System.out.println("phone_number     : " + u.getPhoneNumber());
            System.out.println("is_active        : " + u.getIsActive());
            System.out.println("state            : " + u.getState());
        }

        /* гасим Spring и выходим */
        ctx.close();
    }
}

