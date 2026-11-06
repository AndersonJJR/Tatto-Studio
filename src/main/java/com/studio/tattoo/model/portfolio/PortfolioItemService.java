package com.studio.tattoo.model.portfolio;

import com.studio.tattoo.model.artist.TattooArtist;
import com.studio.tattoo.model.artist.TattooArtistService;
import com.studio.tattoo.model.portfolio.dto.CreatePortfolioItemDTO;
import com.studio.tattoo.model.portfolio.dto.PortfolioItemResponseDTO;
import com.studio.tattoo.model.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioItemService {

    private final PortfolioItemRepository portfolioItemRepository;
    private final TattooArtistService tattooArtistService;

    @Transactional(readOnly = true)
    public List<PortfolioItemResponseDTO> findAll(Long artistId, String styleTag) {
        List<PortfolioItem> items;

        boolean hasArtist = artistId != null;
        boolean hasStyle = styleTag != null && !styleTag.isBlank();

        if (hasArtist && hasStyle) {
            items = portfolioItemRepository.findByArtistIdAndStyleTagIgnoreCase(artistId, styleTag.trim());
        } else if (hasArtist) {
            items = portfolioItemRepository.findByArtistId(artistId);
        } else if (hasStyle) {
            items = portfolioItemRepository.findByStyleTagIgnoreCase(styleTag.trim());
        } else {
            items = portfolioItemRepository.findAll();
        }

        return items.stream().map(PortfolioItemResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public PortfolioItemResponseDTO findById(Long id) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item do portfólio não encontrado com ID: " + id));
        return new PortfolioItemResponseDTO(item);
    }

    @Transactional
    public PortfolioItemResponseDTO create(CreatePortfolioItemDTO dto, User authenticatedUser) {
        TattooArtist artist = tattooArtistService.findEntityByUserEmail(authenticatedUser.getEmail());

        PortfolioItem item = PortfolioItem.builder()
                .title(dto.title())
                .description(dto.description())
                .imageUrl(dto.imageUrl())
                .styleTag(dto.styleTag())
                .artist(artist)
                .build();

        PortfolioItem saved = portfolioItemRepository.save(item);
        return new PortfolioItemResponseDTO(saved);
    }

    @Transactional
    public PortfolioItemResponseDTO update(Long id, CreatePortfolioItemDTO dto, User authenticatedUser) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item do portfólio não encontrado com ID: " + id));

        if (!item.getArtist().getUser().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Você só possui permissão para atualizar itens do seu próprio portfólio.");
        }

        item.setTitle(dto.title());
        item.setDescription(dto.description());
        item.setImageUrl(dto.imageUrl());
        item.setStyleTag(dto.styleTag());

        PortfolioItem updated = portfolioItemRepository.save(item);
        return new PortfolioItemResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id, User authenticatedUser) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item do portfólio não encontrado com ID: " + id));

        if (!item.getArtist().getUser().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Você só possui permissão para remover itens do seu próprio portfólio.");
        }

        portfolioItemRepository.delete(item);
    }
}
