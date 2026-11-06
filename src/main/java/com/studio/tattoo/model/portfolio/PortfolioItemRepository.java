package com.studio.tattoo.model.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {

    List<PortfolioItem> findByArtistId(Long artistId);

    List<PortfolioItem> findByStyleTagIgnoreCase(String styleTag);

    List<PortfolioItem> findByArtistIdAndStyleTagIgnoreCase(Long artistId, String styleTag);
}
