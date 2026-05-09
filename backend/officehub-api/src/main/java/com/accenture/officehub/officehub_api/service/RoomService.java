package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;

import java.util.List;

public interface RoomService {
    List<RoomResponseDto> listRooms();
    RoomStatusResponseDto getRoomStatus(Long roomId);
}
