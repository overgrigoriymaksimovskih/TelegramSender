package com.example.telegramadmin.repository;

import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByTelegramUserId(Long telegramUserId);

    @Query("SELECT new com.example.telegramadmin.dto.NotificationRecipientDto(u.telegramUserId, u.firstName) " +
            "FROM AppUser u WHERE u.telegramUserId = :telegramUserId")
    List<NotificationRecipientDto> findNotificationDtoByTelegramUserId(Long telegramUserId);

    @Query("SELECT new com.example.telegramadmin.dto.NotificationRecipientDto(u.telegramUserId, u.firstName) " +
            "FROM AppUser u")
    List<NotificationRecipientDto> findAllNotificationDto();
}
