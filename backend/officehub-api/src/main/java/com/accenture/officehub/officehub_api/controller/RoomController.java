package com.accenture.officehub.officehub_api.controller;

import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;
import com.accenture.officehub.officehub_api.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomResponseDto>> listRooms() {
        return ResponseEntity.ok(roomService.listRooms());
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<RoomStatusResponseDto> getRoomStatus(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomService.getRoomStatus(id));
    }
}
