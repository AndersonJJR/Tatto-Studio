package com.studio.tattoo.model.portfolio.dto;

import com.studio.tattoo.model.portfolio.PortfolioItem;

import java.time.LocalDateTime;

public record PortfolioItemResponseDTO(
        Long id,
        String title,
        String description,
        String imageUrl,
        String styleTag,
        Long artistId,
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
