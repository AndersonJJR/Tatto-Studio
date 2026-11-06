package com.studio.tattoo.model.appointment.dto;

import com.studio.tattoo.model.appointment.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record UpdateAppointmentDTO(
        @Future(message = "A nova data da sessão deve ser futura")
        LocalDateTime sessionDate,

        @Min(value = 15, message = "A duração mínima da sessão é de 15 minutos")
        Integer estimatedDurationMinutes,

        AppointmentStatus status,

        String designSpecifications
) {}
