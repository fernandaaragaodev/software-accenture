package com.accenture.officehub.officehub_api.dto;

public record TeamMemberResponseDto(
        Long id,
        String displayName,
        String professionalProfile,
        String profileLabel,
        boolean hidePresenceFromTeam
) {}
