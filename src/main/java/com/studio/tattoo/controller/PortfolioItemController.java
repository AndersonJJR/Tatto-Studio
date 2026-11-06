package com.studio.tattoo.controller;

import com.studio.tattoo.model.portfolio.PortfolioItemService;
import com.studio.tattoo.model.portfolio.dto.CreatePortfolioItemDTO;
import com.studio.tattoo.model.portfolio.dto.PortfolioItemResponseDTO;
import com.studio.tattoo.model.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
@Tag(name = "Portfólio", description = "Endpoints para consulta pública e gerenciamento de itens de portfólio por tatuadores")
public class PortfolioItemController {

    private final PortfolioItemService portfolioItemService;

    @GetMapping
    @Operation(summary = "Lista pública dos itens de portfólio, com filtros opcionais por artista e estilo")
    public ResponseEntity<List<PortfolioItemResponseDTO>> findAll(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) String styleTag) {
        return ResponseEntity.ok(portfolioItemService.findAll(artistId, styleTag));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta pública de um item específico do portfólio por ID")
    public ResponseEntity<PortfolioItemResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioItemService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cria um novo item no portfólio do tatuador autenticado")
    public ResponseEntity<PortfolioItemResponseDTO> create(
            @Valid @RequestBody CreatePortfolioItemDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        PortfolioItemResponseDTO response = portfolioItemService.create(dto, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Atualiza um item do portfólio do tatuador autenticado")
    public ResponseEntity<PortfolioItemResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CreatePortfolioItemDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(portfolioItemService.update(id, dto, authenticatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove um item do portfólio do tatuador autenticado")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User authenticatedUser) {
        portfolioItemService.delete(id, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
