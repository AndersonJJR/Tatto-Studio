package com.studio.tattoo.model.appointment;

import com.studio.tattoo.exception.BusinessRuleException;
import com.studio.tattoo.model.appointment.dto.AppointmentResponseDTO;
import com.studio.tattoo.model.appointment.dto.CreateAppointmentDTO;
import com.studio.tattoo.model.appointment.dto.UpdateAppointmentDTO;
import com.studio.tattoo.model.artist.TattooArtist;
import com.studio.tattoo.model.artist.TattooArtistRepository;
import com.studio.tattoo.model.client.Client;
import com.studio.tattoo.model.client.ClientRepository;
import com.studio.tattoo.model.user.Role;
import com.studio.tattoo.model.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TattooArtistRepository tattooArtistRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public AppointmentResponseDTO create(CreateAppointmentDTO dto, User authenticatedUser) {
        Client client = clientRepository.findByUserEmail(authenticatedUser.getEmail())
                .orElseThrow(() -> new BusinessRuleException("Apenas clientes cadastrados podem solicitar agendamentos."));

        TattooArtist artist = tattooArtistRepository.findById(dto.artistId())
                .orElseThrow(() -> new EntityNotFoundException("Tatuador não encontrado com o ID: " + dto.artistId()));

        validateScheduleConflict(artist.getId(), dto.sessionDate(), dto.estimatedDurationMinutes(), null);

        Appointment appointment = Appointment.builder()
                .sessionDate(dto.sessionDate())
                .estimatedDurationMinutes(dto.estimatedDurationMinutes())
                .status(AppointmentStatus.PENDING)
                .designSpecifications(dto.designSpecifications())
                .client(client)
                .artist(artist)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return new AppointmentResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> findMyAppointments(User authenticatedUser) {
        List<Appointment> appointments;

        if (authenticatedUser.getRole() == Role.ROLE_ARTIST) {
            TattooArtist artist = tattooArtistRepository.findByUserEmail(authenticatedUser.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("Perfil de tatuador não encontrado."));
            appointments = appointmentRepository.findByArtistId(artist.getId());
        } else {
            Client client = clientRepository.findByUserEmail(authenticatedUser.getEmail())
                    .orElseThrow(() -> new EntityNotFoundException("Perfil de cliente não encontrado."));
            appointments = appointmentRepository.findByClientId(client.getId());
        }

        return appointments.stream().map(AppointmentResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO findById(Long id, User authenticatedUser) {
        Appointment appointment = findEntityById(id);
        validateAppointmentAccess(appointment, authenticatedUser);
        return new AppointmentResponseDTO(appointment);
    }

    @Transactional
    public AppointmentResponseDTO updateStatus(Long id, AppointmentStatus newStatus, User authenticatedUser) {
        Appointment appointment = findEntityById(id);
        validateAppointmentAccess(appointment, authenticatedUser);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException("Não é permitido alterar o status de um agendamento já cancelado.");
        }

        if (appointment.getStatus() == AppointmentStatus.FINISHED) {
            throw new BusinessRuleException("Não é permitido alterar o status de um agendamento já finalizado.");
        }

        boolean isArtist = appointment.getArtist().getUser().getId().equals(authenticatedUser.getId());
        boolean isClient = appointment.getClient().getUser().getId().equals(authenticatedUser.getId());

        if (newStatus == AppointmentStatus.CANCELLED) {
            // Ambos podem cancelar se forem partes do agendamento
            appointment.setStatus(AppointmentStatus.CANCELLED);
        } else if (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.FINISHED) {
            // Apenas o tatuador pode confirmar ou finalizar a sessão
            if (!isArtist) {
                throw new AccessDeniedException("Apenas o tatuador responsável pode confirmar ou finalizar o agendamento.");
            }
            appointment.setStatus(newStatus);
        } else {
            appointment.setStatus(newStatus);
        }

        Appointment updated = appointmentRepository.save(appointment);
        return new AppointmentResponseDTO(updated);
    }

    @Transactional
    public AppointmentResponseDTO update(Long id, UpdateAppointmentDTO dto, User authenticatedUser) {
        Appointment appointment = findEntityById(id);
        validateAppointmentAccess(appointment, authenticatedUser);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.FINISHED) {
            throw new BusinessRuleException("Agendamentos cancelados ou finalizados não podem ser alterados.");
        }

        LocalDateTime newDate = dto.sessionDate() != null ? dto.sessionDate() : appointment.getSessionDate();
        Integer newDuration = dto.estimatedDurationMinutes() != null ? dto.estimatedDurationMinutes() : appointment.getEstimatedDurationMinutes();

        if (dto.sessionDate() != null || dto.estimatedDurationMinutes() != null) {
            validateScheduleConflict(appointment.getArtist().getId(), newDate, newDuration, appointment.getId());
            appointment.setSessionDate(newDate);
            appointment.setEstimatedDurationMinutes(newDuration);
        }

        if (dto.designSpecifications() != null && !dto.designSpecifications().isBlank()) {
            appointment.setDesignSpecifications(dto.designSpecifications());
        }

        if (dto.status() != null) {
            return updateStatus(id, dto.status(), authenticatedUser);
        }

        Appointment updated = appointmentRepository.save(appointment);
        return new AppointmentResponseDTO(updated);
    }

    @Transactional
    public AppointmentResponseDTO cancel(Long id, User authenticatedUser) {
        return updateStatus(id, AppointmentStatus.CANCELLED, authenticatedUser);
    }

    private Appointment findEntityById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado com o ID: " + id));
    }

    private void validateAppointmentAccess(Appointment appointment, User authenticatedUser) {
        boolean isArtist = appointment.getArtist().getUser().getId().equals(authenticatedUser.getId());
        boolean isClient = appointment.getClient().getUser().getId().equals(authenticatedUser.getId());

        if (!isArtist && !isClient) {
            throw new AccessDeniedException("Você não possui permissão para visualizar ou gerenciar este agendamento.");
        }
    }

    private void validateScheduleConflict(Long artistId, LocalDateTime newStart, Integer durationMinutes, Long excludeId) {
        LocalDateTime newEnd = newStart.plusMinutes(durationMinutes);

        // Busca agendamentos próximos (24h antes e depois)
        List<Appointment> existingAppointments = appointmentRepository.findByArtistIdAndSessionDateBetween(
                artistId,
                newStart.minusDays(1),
                newStart.plusDays(1)
        );

        for (Appointment existing : existingAppointments) {
            if (excludeId != null && existing.getId().equals(excludeId)) {
                continue;
            }
            if (existing.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }

            LocalDateTime existingStart = existing.getSessionDate();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getEstimatedDurationMinutes());

            // Verifica colisão: [newStart, newEnd] sobrepõe [existingStart, existingEnd]
            if (newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd)) {
                throw new BusinessRuleException(
                        "Conflito de agenda: O tatuador já possui um agendamento entre " +
                                existingStart + " e " + existingEnd + "."
                );
            }
        }
    }
}
