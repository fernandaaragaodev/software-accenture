package com.accenture.officehub.officehub_api.dto;

import java.util.List;

public record WorkplaceContextResponseDto(
        boolean employeeRegistered,
        String displayName,
        Long teamId,
        String teamName,
        String teamPreferredFloor,
        String professionalProfile,
        String profileLabel,
        List<ColleagueContextDto> visibleColleagues
) {}
