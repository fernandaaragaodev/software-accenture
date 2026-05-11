package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomPositionResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;

import java.util.List;

public interface RoomService {
    List<RoomResponseDto> listRooms();
    RoomStatusResponseDto getRoomStatus(Long roomId);
    List<RoomPositionResponseDto> listRoomPositions(Long roomId, String date, String start, String end);

    void setRoomBlocked(Long roomId, boolean blocked, String requesterRole, String adminPassword);
}
