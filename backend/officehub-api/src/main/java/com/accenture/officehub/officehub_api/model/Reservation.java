package com.accenture.officehub.officehub_api.model;

import com.accenture.officehub.officehub_api.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class Reservation {
    private Long id;
    private Long roomId;
    private String room;
    private String user;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private ReservationStatus status;

    public Reservation() {}

    public Reservation(Long id, Long roomId, String room, String user, LocalDate date, LocalTime start, LocalTime end, ReservationStatus status) {
        this.id = id;
        this.roomId = roomId;
        this.room = room;
        this.user = user;
        this.date = date;
        this.start = start;
        this.end = end;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStart() { return start; }
    public void setStart(LocalTime start) { this.start = start; }
    public LocalTime getEnd() { return end; }
    public void setEnd(LocalTime end) { this.end = end; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
