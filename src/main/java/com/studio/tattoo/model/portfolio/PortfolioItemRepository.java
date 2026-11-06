package com.studio.tattoo.model.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, UUID> {

    List<PortfolioItem> findByArtistId(UUID artistId);

    List<PortfolioItem> findByStyleTagIgnoreCase(String styleTag);

    List<PortfolioItem> findByArtistIdAndStyleTagIgnoreCase(UUID artistId, String styleTag);
}
