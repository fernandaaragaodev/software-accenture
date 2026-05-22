package com.accenture.officehub.officehub_api.repository;

import com.accenture.officehub.officehub_api.model.Notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    List<Notification> findAll();
    Optional<Notification> findById(Long id);
    Notification save(Notification notification);
}
