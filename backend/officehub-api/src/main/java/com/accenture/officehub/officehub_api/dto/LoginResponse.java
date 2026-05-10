package com.accenture.officehub.officehub_api.dto;

public record LoginResponse(String token, String name, String role, String avatar) {
}
