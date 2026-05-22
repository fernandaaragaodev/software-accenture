package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.ManagerTeamResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomSuggestionResponseDto;
import com.accenture.officehub.officehub_api.dto.WorkplaceContextResponseDto;

import java.util.List;

public interface WorkplaceService {

    WorkplaceContextResponseDto getContextForUser(String userDisplayName);

    List<ManagerTeamResponseDto> listTeamsForManager(String requesterName, String requesterRole);

    List<RoomSuggestionResponseDto> suggestRooms(
            String userDisplayName,
            String date,
            String start,
            String end,
            int limit
    );
}
