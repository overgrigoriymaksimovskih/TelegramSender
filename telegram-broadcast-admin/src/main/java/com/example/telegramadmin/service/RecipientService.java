package com.example.telegramadmin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.telegramadmin.dto.NotificationRecipientDto;
import com.example.telegramadmin.repository.AppUserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipientService {

    private final AppUserRepository appUserRepository;

    @Autowired
    public RecipientService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<NotificationRecipientDto> getRecipientsDtoList() {
        List<NotificationRecipientDto> result = appUserRepository.findAllNotificationDto();
        return new ArrayList<>(result);
    }
}
