package com.studio.tattoo.model.client.dto;

import com.studio.tattoo.model.client.Client;

import java.util.UUID;

public record ClientResponseDTO(
        UUID id,
        UUID userId,
        String name,
        String email,
        String phone,
        String preferences
) {
    public ClientResponseDTO(Client client) {
        this(
                client.getId(),
                client.getUser() != null ? client.getUser().getId() : null,
                client.getName(),
                client.getUser() != null ? client.getUser().getEmail() : null,
                client.getPhone(),
                client.getPreferences()
        );
    }
}
