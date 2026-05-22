package com.accenture.officehub.officehub_api.dto;

import java.util.List;

public record ManagerTeamResponseDto(
        Long id,
        String name,
        String preferredFloor,
        List<TeamMemberResponseDto> members
) {}
