package com.accenture.officehub.officehub_api.controller;

import com.accenture.officehub.officehub_api.dto.RoomBlockRequestDto;
import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomPositionResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;
import com.accenture.officehub.officehub_api.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/{id}/positions")
    public ResponseEntity<List<RoomPositionResponseDto>> listPositions(
            @PathVariable("id") Long id,
            @RequestParam("date") String date,
            @RequestParam("start") String start,
            @RequestParam("end") String end
    ) {
        return ResponseEntity.ok(roomService.listRoomPositions(id, date, start, end));
    }

    @GetMapping("/{id}/positions/overview")
    public ResponseEntity<List<RoomPositionResponseDto>> listPositionsOverview(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roomService.listRoomPositionsOverview(id));
    }

    @PostMapping("/{id}/positions/{code}/block")
    public ResponseEntity<Void> blockPosition(
            @PathVariable("id") Long id,
            @PathVariable("code") String code,
            @RequestParam("requesterRole") String requesterRole
    ) {
        roomService.setPositionBlocked(id, code, true, requesterRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/positions/{code}/unblock")
    public ResponseEntity<Void> unblockPosition(
            @PathVariable("id") Long id,
            @PathVariable("code") String code,
            @RequestParam("requesterRole") String requesterRole
    ) {
        roomService.setPositionBlocked(id, code, false, requesterRole);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockRoom(
            @PathVariable("id") Long id,
            @RequestParam("requesterRole") String requesterRole,
            @Valid @RequestBody RoomBlockRequestDto body
    ) {
        roomService.setRoomBlocked(id, true, requesterRole, body.adminPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblockRoom(
            @PathVariable("id") Long id,
            @RequestParam("requesterRole") String requesterRole
    ) {
        roomService.setRoomBlocked(id, false, requesterRole, null);
        return ResponseEntity.noContent().build();
    }
}
