package com.studio.tattoo.model.appointment.dto;

import com.studio.tattoo.model.appointment.Appointment;
import com.studio.tattoo.model.appointment.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
        Long id,
        LocalDateTime sessionDate,
        Integer estimatedDurationMinutes,
        AppointmentStatus status,
        String designSpecifications,
        Long clientId,
        String clientName,
        String clientPhone,
        Long artistId,
        String artistName,
        LocalDateTime createdAt
) {
    public AppointmentResponseDTO(Appointment appointment) {
        this(
                appointment.getId(),
                appointment.getSessionDate(),
                appointment.getEstimatedDurationMinutes(),
                appointment.getStatus(),
                appointment.getDesignSpecifications(),
                appointment.getClient() != null ? appointment.getClient().getId() : null,
                appointment.getClient() != null ? appointment.getClient().getName() : null,
                appointment.getClient() != null ? appointment.getClient().getPhone() : null,
                appointment.getArtist() != null ? appointment.getArtist().getId() : null,
                appointment.getArtist() != null ? appointment.getArtist().getName() : null,
                appointment.getCreatedAt()
        );
    }
}
