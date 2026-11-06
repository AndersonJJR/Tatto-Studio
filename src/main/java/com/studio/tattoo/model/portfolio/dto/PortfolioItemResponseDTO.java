package com.studio.tattoo.model.portfolio.dto;

import com.studio.tattoo.model.portfolio.PortfolioItem;

import java.time.LocalDateTime;
import java.util.UUID;

public record PortfolioItemResponseDTO(
        UUID id,
        String title,
        String description,
        String imageUrl,
        String styleTag,
        UUID artistId,
        String artistName,
        LocalDateTime createdAt
) {
    public PortfolioItemResponseDTO(PortfolioItem item) {
        this(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImageUrl(),
                item.getStyleTag(),
                item.getArtist() != null ? item.getArtist().getId() : null,
                item.getArtist() != null ? item.getArtist().getName() : null,
                item.getCreatedAt()
        );
    }
}
