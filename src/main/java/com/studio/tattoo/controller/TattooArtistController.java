package com.studio.tattoo.controller;

import com.studio.tattoo.model.artist.TattooArtistService;
import com.studio.tattoo.model.artist.dto.CreateTattooArtistDTO;
import com.studio.tattoo.model.artist.dto.TattooArtistResponseDTO;
import com.studio.tattoo.model.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artists")
@RequiredArgsConstructor
@Tag(name = "Tatuadores", description = "Endpoints para consulta e atualização de perfis de tatuadores")
public class TattooArtistController {

    private final TattooArtistService tattooArtistService;

    @GetMapping
    @Operation(summary = "Lista todos os tatuadores, com filtro opcional por especialidade")
    public ResponseEntity<List<TattooArtistResponseDTO>> findAll(
            @RequestParam(required = false) String specialty) {
        return ResponseEntity.ok(tattooArtistService.findAll(specialty));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca os detalhes de um tatuador pelo ID")
    public ResponseEntity<TattooArtistResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tattooArtistService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ARTIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Atualiza o perfil do tatuador autenticado")
    public ResponseEntity<TattooArtistResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateTattooArtistDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(tattooArtistService.update(id, dto, authenticatedUser));
    }
}
