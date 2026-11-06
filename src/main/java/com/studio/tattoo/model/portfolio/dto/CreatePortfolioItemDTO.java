package com.studio.tattoo.model.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePortfolioItemDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 2, max = 150, message = "O título deve conter entre 2 e 150 caracteres")
        String title,

        String description,

        @NotBlank(message = "A URL da imagem é obrigatória")
        String imageUrl,

        @NotBlank(message = "A tag de estilo é obrigatória (ex: Old School, Realismo, Fineline, Oriental)")
        String styleTag
) {}
