package com.studio.tattoo.model.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateAppointmentDTO(
        @NotNull(message = "O ID do tatuador é obrigatório")
        Long artistId,

        @NotNull(message = "A data e hora da sessão são obrigatórias")
        @Future(message = "A data da sessão deve ser futura")
        LocalDateTime sessionDate,

        @NotNull(message = "A duração estimada em minutos é obrigatória")
        @Min(value = 15, message = "A duração mínima da sessão é de 15 minutos")
        Integer estimatedDurationMinutes,

        @NotBlank(message = "As especificações do desenho são obrigatórias")
        String designSpecifications
) {}
