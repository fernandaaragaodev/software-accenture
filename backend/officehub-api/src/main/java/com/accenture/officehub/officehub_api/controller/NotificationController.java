package com.accenture.officehub.officehub_api.controller;

import com.accenture.officehub.officehub_api.dto.NotificationResponseDto;
import com.accenture.officehub.officehub_api.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> listNotifications(
            @RequestParam(value = "viewerName", required = false) String viewerName,
            @RequestParam(value = "viewerRole", required = false) String viewerRole
    ) {
        return ResponseEntity.ok(notificationService.listNotifications(viewerName, viewerRole));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable("id") Long id,
            @RequestParam(value = "viewerName", required = false) String viewerName,
            @RequestParam(value = "viewerRole", required = false) String viewerRole
    ) {
        notificationService.markAsRead(id, viewerName, viewerRole);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam(value = "viewerName", required = false) String viewerName,
            @RequestParam(value = "viewerRole", required = false) String viewerRole
    ) {
        notificationService.markAllAsRead(viewerName, viewerRole);
        return ResponseEntity.noContent().build();
    }
}
