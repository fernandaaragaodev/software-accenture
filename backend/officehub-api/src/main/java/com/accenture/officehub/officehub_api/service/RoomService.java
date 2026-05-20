package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.AvailablePositionTypeDto;
import com.accenture.officehub.officehub_api.dto.AvailableRoomForReservationDto;
import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomPositionResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;

import java.util.List;

public interface RoomService {
    List<RoomResponseDto> listRooms();
    RoomStatusResponseDto getRoomStatus(Long roomId);
    List<AvailablePositionTypeDto> listAvailablePositionTypes(String date, String start, String end);
    List<AvailableRoomForReservationDto> listAvailableRoomsForReservation(
            String date, String start, String end, String seatType);
    List<RoomPositionResponseDto> listRoomPositions(Long roomId, String date, String start, String end);

    List<RoomPositionResponseDto> listRoomPositionsOverview(Long roomId);

    void setPositionBlocked(Long roomId, String positionCode, boolean blocked, String requesterRole);

    void setRoomBlocked(Long roomId, boolean blocked, String requesterRole, String adminPassword);
}
