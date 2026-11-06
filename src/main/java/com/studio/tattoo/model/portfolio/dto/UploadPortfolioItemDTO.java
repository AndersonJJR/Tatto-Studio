package com.studio.tattoo.model.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UploadPortfolioItemDTO(
        @NotBlank(message = "O título é obrigatório")
        @Size(min = 2, max = 150, message = "O título deve conter entre 2 e 150 caracteres")
        String title,

        String description,

        String styleTag,

        String authorCredits,

        Boolean isPublic,

        @NotNull(message = "O arquivo de imagem é obrigatório")
        MultipartFile file
) {}
