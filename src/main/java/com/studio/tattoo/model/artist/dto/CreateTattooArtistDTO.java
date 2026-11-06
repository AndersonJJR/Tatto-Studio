package com.studio.tattoo.model.artist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTattooArtistDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve conter entre 2 e 100 caracteres")
        String name,

        String phone,

        @NotBlank(message = "A especialidade/estilo é obrigatória")
        String specialty,

        String bio
) {}
