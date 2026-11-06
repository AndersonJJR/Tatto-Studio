package com.studio.tattoo.controller;

import com.studio.tattoo.model.appointment.AppointmentService;
import com.studio.tattoo.model.appointment.AppointmentStatus;
import com.studio.tattoo.model.appointment.dto.AppointmentResponseDTO;
import com.studio.tattoo.model.appointment.dto.CreateAppointmentDTO;
import com.studio.tattoo.model.appointment.dto.UpdateAppointmentDTO;
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
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Endpoints para solicitação, consulta e gerenciamento de agendamentos de sessões")
@SecurityRequirement(name = "Bearer Authentication")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Solicita um novo agendamento de tatuagem (Exclusivo para clientes)")
    public ResponseEntity<AppointmentResponseDTO> create(
            @Valid @RequestBody CreateAppointmentDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        AppointmentResponseDTO response = appointmentService.create(dto, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todos os agendamentos do usuário autenticado (visão de cliente ou tatuador)")
    public ResponseEntity<List<AppointmentResponseDTO>> findMyAppointments(
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(appointmentService.findMyAppointments(authenticatedUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca detalhes de um agendamento específico")
    public ResponseEntity<AppointmentResponseDTO> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(appointmentService.findById(id, authenticatedUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza os dados de um agendamento")
    public ResponseEntity<AppointmentResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentDTO dto,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(appointmentService.update(id, dto, authenticatedUser));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza o status do agendamento (Ex: CONFIRMED pelo tatuador, CANCELLED por cliente/tatuador)")
    public ResponseEntity<AppointmentResponseDTO> updateStatus(
            @PathVariable UUID id,
            @RequestParam AppointmentStatus status,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status, authenticatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancela um agendamento")
    public ResponseEntity<AppointmentResponseDTO> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(appointmentService.cancel(id, authenticatedUser));
    }
}
