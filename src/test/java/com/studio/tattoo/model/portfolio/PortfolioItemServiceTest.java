package com.studio.tattoo.model.portfolio;

import com.studio.tattoo.exception.BusinessRuleException;
import com.studio.tattoo.model.artist.TattooArtist;
import com.studio.tattoo.model.artist.TattooArtistService;
import com.studio.tattoo.model.portfolio.dto.CreatePortfolioItemDTO;
import com.studio.tattoo.model.portfolio.dto.PortfolioItemResponseDTO;
import com.studio.tattoo.model.portfolio.dto.UploadPortfolioItemDTO;
import com.studio.tattoo.model.user.Role;
import com.studio.tattoo.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    private final String testUploadDir = "target/test-uploads";

    @BeforeEach
    void setUp() {
        artistId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        ReflectionTestUtils.setField(portfolioItemService, "uploadDir", testUploadDir);

        artistUser = User.builder().id(UUID.randomUUID()).name("Beto Ink").email("beto@studio.com").role(Role.ROLE_ARTIST).build();
        otherArtistUser = User.builder().id(UUID.randomUUID()).name("Outro Tatuador").email("outro@studio.com").role(Role.ROLE_ARTIST).build();

        artist = TattooArtist.builder().id(artistId).user(artistUser).name("Beto Ink").build();
        otherArtist = TattooArtist.builder().id(UUID.randomUUID()).user(otherArtistUser).name("Outro Tatuador").build();
    }

    @Test
    @DisplayName("Deve realizar upload de arquivo físico e criar o item de portfólio com sucesso")
    void shouldUploadPortfolioItemSuccessfully() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "tattoo.png",
                "image/png",
                "fake image content bytes".getBytes()
        );

        UploadPortfolioItemDTO dto = new UploadPortfolioItemDTO(
                "Dragão Oriental",
                "Trabalho autoral nas costas",
                "Oriental",
                "Beto Ink & Convidados",
                true,
                file
        );

        when(tattooArtistService.findEntityByUserEmail(artistUser.getEmail())).thenReturn(artist);

        PortfolioItem savedItem = PortfolioItem.builder()
                .id(itemId)
                .title(dto.title())
                .description(dto.description())
                .imageUrl("/uploads/dummy.png")
                .styleTag(dto.styleTag())
                .authorCredits(dto.authorCredits())
                .isPublic(dto.isPublic())
                .artist(artist)
                .createdAt(LocalDateTime.now())
                .build();

        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(savedItem);

        PortfolioItemResponseDTO response = portfolioItemService.upload(dto, artistUser);

        assertNotNull(response);
        assertEquals(itemId, response.id());
        assertEquals("Dragão Oriental", response.title());
        assertEquals("Beto Ink & Convidados", response.authorCredits());
        assertTrue(response.isPublic());
        verify(portfolioItemRepository).save(any(PortfolioItem.class));
    }

    @Test
    @DisplayName("Deve rejeitar arquivo com extensão não permitida (ex: .pdf)")
    void shouldRejectUploadWithInvalidExtension() {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "documento.pdf",
                "application/pdf",
                "fake pdf content".getBytes()
        );

        UploadPortfolioItemDTO dto = new UploadPortfolioItemDTO(
                "Tentativa inválida",
                "Desc",
                "Fineline",
                null,
                true,
                invalidFile
        );

        when(tattooArtistService.findEntityByUserEmail(artistUser.getEmail())).thenReturn(artist);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                portfolioItemService.upload(dto, artistUser)
        );

        assertTrue(ex.getMessage().contains("Extensão de arquivo inválida"));
        verify(portfolioItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void shouldRejectEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        UploadPortfolioItemDTO dto = new UploadPortfolioItemDTO(
                "Arquivo Vazio",
                "Desc",
                "Realismo",
                null,
                true,
                emptyFile
        );

        when(tattooArtistService.findEntityByUserEmail(artistUser.getEmail())).thenReturn(artist);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                portfolioItemService.upload(dto, artistUser)
        );

        assertTrue(ex.getMessage().contains("não pode estar vazio"));
        verify(portfolioItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar arquivo maior que 5 MB")
    void shouldRejectFileExceedingSizeLimit() {
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 1]; // 5 MB + 1 byte
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "huge.png",
                "image/png",
                largeBytes
        );

        UploadPortfolioItemDTO dto = new UploadPortfolioItemDTO(
                "Arquivo Gigante",
                "Desc",
                "Realismo",
                null,
                true,
                largeFile
        );

        when(tattooArtistService.findEntityByUserEmail(artistUser.getEmail())).thenReturn(artist);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () ->
                portfolioItemService.upload(dto, artistUser)
        );

        assertTrue(ex.getMessage().contains("limite máximo permitido de 5 MB"));
        verify(portfolioItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar um item de portfólio com sucesso via JSON para o tatuador autenticado")
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
