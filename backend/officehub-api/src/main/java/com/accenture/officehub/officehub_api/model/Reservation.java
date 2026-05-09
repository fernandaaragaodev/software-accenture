package com.accenture.officehub.officehub_api.model;

import com.accenture.officehub.officehub_api.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Reservation {
    private Long id;
    private Long roomId;
    private String room;
    private String user;
    private String requesterRole;
    private String reservationGroupId;
    private String seatCode;
    private String seatType;
    private List<String> requestedEquipment;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private ReservationStatus status;

    public Reservation() {
        this.requestedEquipment = new ArrayList<>();
    }

    public Reservation(
            Long id,
            Long roomId,
            String room,
            String user,
            String requesterRole,
            String reservationGroupId,
            String seatCode,
            String seatType,
            List<String> requestedEquipment,
            LocalDate date,
            LocalTime start,
            LocalTime end,
            ReservationStatus status
    ) {
        this.id = id;
        this.roomId = roomId;
        this.room = room;
        this.user = user;
        this.requesterRole = requesterRole;
        this.reservationGroupId = reservationGroupId;
        this.seatCode = seatCode;
        this.seatType = seatType;
        this.requestedEquipment = requestedEquipment == null ? new ArrayList<>() : new ArrayList<>(requestedEquipment);
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
    public String getRequesterRole() { return requesterRole; }
    public void setRequesterRole(String requesterRole) { this.requesterRole = requesterRole; }
    public String getReservationGroupId() { return reservationGroupId; }
    public void setReservationGroupId(String reservationGroupId) { this.reservationGroupId = reservationGroupId; }
    public String getSeatCode() { return seatCode; }
    public void setSeatCode(String seatCode) { this.seatCode = seatCode; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
    public List<String> getRequestedEquipment() { return requestedEquipment; }
    public void setRequestedEquipment(List<String> requestedEquipment) {
        this.requestedEquipment = requestedEquipment == null ? new ArrayList<>() : new ArrayList<>(requestedEquipment);
    }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStart() { return start; }
    public void setStart(LocalTime start) { this.start = start; }
    public LocalTime getEnd() { return end; }
    public void setEnd(LocalTime end) { this.end = end; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
