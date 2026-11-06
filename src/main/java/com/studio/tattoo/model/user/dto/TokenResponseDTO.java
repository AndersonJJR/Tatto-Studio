package com.studio.tattoo.model.user.dto;

import java.util.UUID;

public record TokenResponseDTO(
        String token,
        String type,
        UUID userId,
        String name,
        String email,
        String role
) {
    public TokenResponseDTO(String token, UUID userId, String name, String email, String role) {
        this(token, "Bearer", userId, name, email, role);
    }
}
