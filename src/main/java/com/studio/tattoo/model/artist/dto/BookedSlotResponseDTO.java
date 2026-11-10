package com.studio.tattoo.model.artist.dto;

import com.studio.tattoo.model.appointment.Appointment;

import java.time.LocalDateTime;

public record BookedSlotResponseDTO(
        LocalDateTime sessionDate,
        Integer estimatedDurationMinutes
) {
    public BookedSlotResponseDTO(Appointment appointment) {
        this(appointment.getSessionDate(), appointment.getEstimatedDurationMinutes());
    }
}
