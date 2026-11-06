package com.studio.tattoo.model.user.dto;

public record TokenResponseDTO(
        String token,
        String type,
        Long userId,
        String name,
        String email,
        String role
) {
    public TokenResponseDTO(String token, Long userId, String name, String email, String role) {
        this(token, "Bearer", userId, name, email, role);
    }
}
