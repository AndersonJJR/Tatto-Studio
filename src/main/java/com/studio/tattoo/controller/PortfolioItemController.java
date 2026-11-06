package com.studio.tattoo.controller;

import com.studio.tattoo.model.portfolio.PortfolioItemService;
import com.studio.tattoo.model.portfolio.dto.CreatePortfolioItemDTO;
import com.studio.tattoo.model.portfolio.dto.PortfolioItemResponseDTO;
import com.studio.tattoo.model.portfolio.dto.UploadPortfolioItemDTO;
import com.studio.tattoo.model.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfólio", description = "Endpoints para consulta pública e gerenciamento de imagens e itens de portfólio por tatuadores")
public class PortfolioItemController {

    private final PortfolioItemService portfolioItemService;

    @GetMapping
    @Operation(summary = "Lista pública dos itens de portfólio, com filtros opcionais por artista e estilo")
    public ResponseEntity<List<PortfolioItemResponseDTO>> findAll(
            @RequestParam(required = false) UUID artistId,
            @RequestParam(required = false) String styleTag) {
        return ResponseEntity.ok(portfolioItemService.findAll(artistId, styleTag));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta pública de um item específico do portfólio por ID")
    public ResponseEntity<PortfolioItemResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(portfolioItemService.findById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Realiza o upload de arquivo de imagem físico e cadastra o item no portfólio")
    public ResponseEntity<PortfolioItemResponseDTO> upload(
            @Valid @ModelAttribute UploadPortfolioItemDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        PortfolioItemResponseDTO response = portfolioItemService.upload(dto, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cria um item no portfólio via payload JSON (com URL externa de imagem)")
    public ResponseEntity<PortfolioItemResponseDTO> create(
            @Valid @RequestBody CreatePortfolioItemDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        PortfolioItemResponseDTO response = portfolioItemService.create(dto, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Atualiza os metadados de um item do portfólio do tatuador autenticado")
    public ResponseEntity<PortfolioItemResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePortfolioItemDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(portfolioItemService.update(id, dto, authenticatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove um item do portfólio e apaga seu arquivo físico")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User authenticatedUser) {
        portfolioItemService.delete(id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
