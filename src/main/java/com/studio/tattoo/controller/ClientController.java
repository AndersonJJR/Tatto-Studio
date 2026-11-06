package com.studio.tattoo.controller;

import com.studio.tattoo.model.client.ClientService;
import com.studio.tattoo.model.client.dto.ClientResponseDTO;
import com.studio.tattoo.model.client.dto.CreateClientDTO;
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

import java.util.UUID;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoints para consulta e gerenciamento de perfis de clientes")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Retorna o perfil do cliente atualmente autenticado")
    public ResponseEntity<ClientResponseDTO> getProfile(@AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(clientService.getProfile(authenticatedUser));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Atualiza o perfil do cliente atualmente autenticado")
    public ResponseEntity<ClientResponseDTO> updateProfile(
            @Valid @RequestBody CreateClientDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(clientService.update(dto, authenticatedUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca o perfil de um cliente pelo ID")
    public ResponseEntity<ClientResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(clientService.findById(id));
    }
}
