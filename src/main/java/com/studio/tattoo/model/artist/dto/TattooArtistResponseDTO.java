package com.studio.tattoo.model.artist.dto;

import com.studio.tattoo.model.artist.TattooArtist;

public record TattooArtistResponseDTO(
        Long id,
        Long userId,
        String name,
        String email,
        String phone,
        String specialty,
        String bio
) {
    public TattooArtistResponseDTO(TattooArtist artist) {
        this(
                artist.getId(),
                artist.getUser() != null ? artist.getUser().getId() : null,
                artist.getName(),
                artist.getUser() != null ? artist.getUser().getEmail() : null,
                artist.getPhone(),
                artist.getSpecialty(),
                artist.getBio()
        );
    }
}
