package com.studio.tattoo.model.portfolio;

import com.studio.tattoo.model.artist.TattooArtist;
import com.studio.tattoo.model.artist.TattooArtistService;
import com.studio.tattoo.model.portfolio.dto.CreatePortfolioItemDTO;
import com.studio.tattoo.model.portfolio.dto.PortfolioItemResponseDTO;
import com.studio.tattoo.model.user.Role;
import com.studio.tattoo.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioItemServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private TattooArtistService tattooArtistService;

    @InjectMocks
    private PortfolioItemService portfolioItemService;

    private User artistUser;
    private User otherArtistUser;
    private TattooArtist artist;
    private TattooArtist otherArtist;
    private UUID artistId;
    private UUID itemId;

    @BeforeEach
    void setUp() {
        artistId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        artistUser = User.builder().id(UUID.randomUUID()).name("Beto Ink").email("beto@studio.com").role(Role.ROLE_ARTIST).build();
        otherArtistUser = User.builder().id(UUID.randomUUID()).name("Outro Tatuador").email("outro@studio.com").role(Role.ROLE_ARTIST).build();

        artist = TattooArtist.builder().id(artistId).user(artistUser).name("Beto Ink").build();
        otherArtist = TattooArtist.builder().id(UUID.randomUUID()).user(otherArtistUser).name("Outro Tatuador").build();
    }

    @Test
    @DisplayName("Deve criar um item de portfólio com sucesso para o tatuador autenticado")
    void shouldCreatePortfolioItemSuccessfully() {
        CreatePortfolioItemDTO dto = new CreatePortfolioItemDTO(
                "Tatuagem Oriental Samurai",
                "Feita em 3 sessões",
                "https://images.example.com/samurai.jpg",
                "Oriental"
        );

        when(tattooArtistService.findEntityByUserEmail(artistUser.getEmail())).thenReturn(artist);

        PortfolioItem savedItem = PortfolioItem.builder()
                .id(itemId)
                .title(dto.title())
                .description(dto.description())
                .imageUrl(dto.imageUrl())
                .styleTag(dto.styleTag())
                .artist(artist)
                .createdAt(LocalDateTime.now())
                .build();

        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(savedItem);

        PortfolioItemResponseDTO response = portfolioItemService.create(dto, artistUser);

        assertNotNull(response);
        assertEquals(itemId, response.id());
        assertEquals("Tatuagem Oriental Samurai", response.title());
        assertEquals("Oriental", response.styleTag());
        assertEquals(artistId, response.artistId());
        verify(portfolioItemRepository).save(any(PortfolioItem.class));
    }

    @Test
    @DisplayName("Deve impedir a alteração de item de portfólio pertencente a outro artista")
    void shouldDenyUpdateFromDifferentArtist() {
        PortfolioItem existingItem = PortfolioItem.builder()
                .id(itemId)
                .title("Tatuagem da Ana")
                .imageUrl("https://images.example.com/item.jpg")
                .styleTag("Fineline")
                .artist(otherArtist)
                .build();

        when(portfolioItemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        CreatePortfolioItemDTO updateDto = new CreatePortfolioItemDTO(
                "Tentativa de Hack",
                "Desc",
                "https://images.example.com/hack.jpg",
                "Fineline"
        );

        assertThrows(AccessDeniedException.class, () ->
                portfolioItemService.update(itemId, updateDto, artistUser)
        );

        verify(portfolioItemRepository, never()).save(any(PortfolioItem.class));
    }
}
