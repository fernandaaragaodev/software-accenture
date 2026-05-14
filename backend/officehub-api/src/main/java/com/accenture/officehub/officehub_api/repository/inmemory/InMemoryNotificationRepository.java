package com.accenture.officehub.officehub_api.repository.inmemory;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.accenture.officehub.officehub_api.model.Notification;
import com.accenture.officehub.officehub_api.repository.NotificationRepository;

@Repository
public class InMemoryNotificationRepository implements NotificationRepository {

    private final List<Notification> notifications = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public List<Notification> findAll() {
        return notifications.stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(this::copy)
                .toList();
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notifications.stream()
                .filter(notification -> notification.getId().equals(id))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public Notification save(Notification notification) {
        Notification mutable = copy(notification);
        if (mutable.getId() == null) {
            mutable.setId(sequence.getAndIncrement());
        }
        notifications.removeIf(current -> current.getId().equals(mutable.getId()));
        notifications.add(mutable);
        return copy(mutable);
    }

    private Notification copy(Notification notification) {
        return new Notification(
                notification.getId(),
                notification.getType(),
                notification.getText(),
                notification.getColor(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReservationId(),
                notification.getReservationGroupId(),
                notification.getActorUserName()
        );
    }
}
