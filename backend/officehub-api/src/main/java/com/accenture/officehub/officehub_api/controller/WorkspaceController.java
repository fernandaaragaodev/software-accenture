package com.accenture.officehub.officehub_api.controller;

import com.accenture.officehub.officehub_api.dto.RoomSuggestionResponseDto;
import com.accenture.officehub.officehub_api.dto.WorkplaceContextResponseDto;
import com.accenture.officehub.officehub_api.service.WorkplaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspace")
public class WorkspaceController {

    private final WorkplaceService workplaceService;

    public WorkspaceController(WorkplaceService workplaceService) {
        this.workplaceService = workplaceService;
    }

    @GetMapping("/context")
    public ResponseEntity<WorkplaceContextResponseDto> getContext(
            @RequestParam("userName") String userName
    ) {
        return ResponseEntity.ok(workplaceService.getContextForUser(userName));
    }

    @GetMapping("/room-suggestions")
    public ResponseEntity<List<RoomSuggestionResponseDto>> suggestRooms(
            @RequestParam("userName") String userName,
            @RequestParam("date") String date,
            @RequestParam("start") String start,
            @RequestParam("end") String end,
            @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(workplaceService.suggestRooms(userName, date, start, end, limit));
    }
}
