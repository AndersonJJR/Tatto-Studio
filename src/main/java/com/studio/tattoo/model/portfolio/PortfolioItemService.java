package com.studio.tattoo.model.portfolio;

import com.studio.tattoo.exception.BusinessRuleException;
import com.studio.tattoo.model.artist.TattooArtist;
import com.studio.tattoo.model.artist.TattooArtistService;
import com.studio.tattoo.model.portfolio.dto.CreatePortfolioItemDTO;
import com.studio.tattoo.model.portfolio.dto.PortfolioItemResponseDTO;
import com.studio.tattoo.model.portfolio.dto.UploadPortfolioItemDTO;
import com.studio.tattoo.model.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioItemService {

    private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg", "image/pjpeg");

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final PortfolioItemRepository portfolioItemRepository;
    private final TattooArtistService tattooArtistService;

    @Transactional(readOnly = true)
    public List<PortfolioItemResponseDTO> findAll(UUID artistId, String styleTag) {
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
    public PortfolioItemResponseDTO findById(UUID id) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item do portfólio não encontrado com ID: " + id));
        return new PortfolioItemResponseDTO(item);
    }

    @Transactional
    public PortfolioItemResponseDTO upload(UploadPortfolioItemDTO dto, User authenticatedUser) {
        TattooArtist artist = tattooArtistService.findEntityByUserEmail(authenticatedUser.getEmail());

        MultipartFile file = dto.file();
        validateFile(file);

        String relativePath = storeFile(file);

        String authorCredits = (dto.authorCredits() != null && !dto.authorCredits().isBlank())
                ? dto.authorCredits().trim()
                : artist.getName();

        boolean isPublic = dto.isPublic() == null || dto.isPublic();

        PortfolioItem item = PortfolioItem.builder()
                .title(dto.title())
                .description(dto.description())
                .imageUrl(relativePath)
                .styleTag(dto.styleTag())
                .authorCredits(authorCredits)
                .isPublic(isPublic)
                .artist(artist)
                .build();

        PortfolioItem saved = portfolioItemRepository.save(item);
        return new PortfolioItemResponseDTO(saved);
    }

    @Transactional
    public PortfolioItemResponseDTO create(CreatePortfolioItemDTO dto, User authenticatedUser) {
        TattooArtist artist = tattooArtistService.findEntityByUserEmail(authenticatedUser.getEmail());

        String authorCredits = (dto.authorCredits() != null && !dto.authorCredits().isBlank())
                ? dto.authorCredits().trim()
                : artist.getName();

        boolean isPublic = dto.isPublic() == null || dto.isPublic();

        PortfolioItem item = PortfolioItem.builder()
                .title(dto.title())
                .description(dto.description())
                .imageUrl(dto.imageUrl())
                .styleTag(dto.styleTag())
                .authorCredits(authorCredits)
                .isPublic(isPublic)
                .artist(artist)
                .build();

        PortfolioItem saved = portfolioItemRepository.save(item);
        return new PortfolioItemResponseDTO(saved);
    }

    @Transactional
    public PortfolioItemResponseDTO update(UUID id, CreatePortfolioItemDTO dto, User authenticatedUser) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item do portfólio não encontrado com ID: " + id));

        if (!item.getArtist().getUser().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Você só possui permissão para atualizar itens do seu próprio portfólio.");
        }

        item.setTitle(dto.title());
        item.setDescription(dto.description());
        item.setImageUrl(dto.imageUrl());
        item.setStyleTag(dto.styleTag());
        if (dto.authorCredits() != null) {
            item.setAuthorCredits(dto.authorCredits().trim());
        }
        if (dto.isPublic() != null) {
            item.setIsPublic(dto.isPublic());
        }

        PortfolioItem updated = portfolioItemRepository.save(item);
        return new PortfolioItemResponseDTO(updated);
    }

    @Transactional
    public void delete(UUID id, User authenticatedUser) {
        PortfolioItem item = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item do portfólio não encontrado com ID: " + id));

        if (!item.getArtist().getUser().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Você só possui permissão para remover itens do seu próprio portfólio.");
        }

        // Tenta remover o arquivo físico se for um upload local
        if (item.getImageUrl() != null && item.getImageUrl().startsWith("/uploads/")) {
            String fileName = item.getImageUrl().substring("/uploads/".length());
            try {
                Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {
                // Não falha a exclusão no banco se houver problema na remoção do arquivo
            }
        }

        portfolioItemRepository.delete(item);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("O arquivo de imagem é obrigatório e não pode estar vazio.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessRuleException("O tamanho do arquivo excede o limite máximo permitido de 5 MB.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessRuleException("Nome de arquivo inválido. Deve possuir extensão.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessRuleException("Extensão de arquivo inválida. Permitido apenas: .png, .jpg, .jpeg.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessRuleException("Tipo de arquivo inválido. Apenas imagens PNG e JPEG são aceitas.");
        }
    }

    private String storeFile(MultipartFile file) {
        try {
            Path targetDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(targetDirectory)) {
                Files.createDirectories(targetDirectory);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase()
                    : ".jpg";

            String uniqueFilename = UUID.randomUUID() + extension;
            Path destinationFile = targetDirectory.resolve(uniqueFilename).normalize();

            // Proteção contra path traversal
            if (!destinationFile.getParent().equals(targetDirectory)) {
                throw new BusinessRuleException("Caminho de arquivo inválido.");
            }

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + uniqueFilename;
        } catch (IOException ex) {
            throw new BusinessRuleException("Falha ao salvar o arquivo fisicamente no servidor: " + ex.getMessage());
        }
    }
}
