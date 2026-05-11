package com.accenture.officehub.officehub_api.enums;

public enum RoomStatus {
    available,
    /** Sala bloqueada por administrador — exibida como indisponível na UI. */
    unavailable,
    reserved,
    occupied
}
