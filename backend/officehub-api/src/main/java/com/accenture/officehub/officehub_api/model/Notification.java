package com.accenture.officehub.officehub_api.model;

import java.time.LocalDateTime;

public class Notification {
    private Long id;
    private String type;
    private String text;
    private String color;
    private boolean read;
    private LocalDateTime createdAt;
    private Long reservationId;
    private String reservationGroupId;

    public Notification() {}

    public Notification(
            Long id,
            String type,
            String text,
            String color,
            boolean read,
            LocalDateTime createdAt,
            Long reservationId,
            String reservationGroupId
    ) {
        this.id = id;
        this.type = type;
        this.text = text;
        this.color = color;
        this.read = read;
        this.createdAt = createdAt;
        this.reservationId = reservationId;
        this.reservationGroupId = reservationGroupId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public String getReservationGroupId() { return reservationGroupId; }
    public void setReservationGroupId(String reservationGroupId) { this.reservationGroupId = reservationGroupId; }
}
