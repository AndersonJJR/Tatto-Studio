package com.studio.tattoo.model.artist;

import com.studio.tattoo.model.appointment.Appointment;
import com.studio.tattoo.model.appointment.AppointmentRepository;
import com.studio.tattoo.model.appointment.AppointmentStatus;
import com.studio.tattoo.model.artist.dto.BookedSlotResponseDTO;
import com.studio.tattoo.model.artist.dto.CreateTattooArtistDTO;
import com.studio.tattoo.model.artist.dto.TattooArtistResponseDTO;
import com.studio.tattoo.model.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TattooArtistService {

    private final TattooArtistRepository tattooArtistRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<TattooArtistResponseDTO> findAll(String specialty) {
        List<TattooArtist> artists;
        if (specialty != null && !specialty.isBlank()) {
            artists = tattooArtistRepository.findBySpecialtyContainingIgnoreCase(specialty.trim());
        } else {
            artists = tattooArtistRepository.findAll();
        }
        return artists.stream().map(TattooArtistResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public TattooArtistResponseDTO findById(UUID id) {
        return new TattooArtistResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public TattooArtistResponseDTO getProfile(User authenticatedUser) {
        TattooArtist artist = findEntityByUserEmail(authenticatedUser.getEmail());
        return new TattooArtistResponseDTO(artist);
    }

    @Transactional(readOnly = true)
    public TattooArtist findEntityById(UUID id) {
        return tattooArtistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tatuador não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public TattooArtist findEntityByUserEmail(String email) {
        return tattooArtistRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Perfil de tatuador não encontrado para o e-mail: " + email));
    }

    @Transactional
    public TattooArtistResponseDTO updateProfile(CreateTattooArtistDTO dto, User authenticatedUser) {
        TattooArtist artist = findEntityByUserEmail(authenticatedUser.getEmail());

        artist.setName(dto.name());
        artist.setPhone(dto.phone());
        artist.setSpecialty(dto.specialty());
        artist.setBio(dto.bio());

        TattooArtist updated = tattooArtistRepository.save(artist);
        return new TattooArtistResponseDTO(updated);
    }

    @Transactional
    public TattooArtistResponseDTO update(UUID id, CreateTattooArtistDTO dto, User authenticatedUser) {
        TattooArtist artist = findEntityById(id);

        if (!artist.getUser().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Você só pode atualizar o seu próprio perfil de tatuador.");
        }

        artist.setName(dto.name());
        artist.setPhone(dto.phone());
        artist.setSpecialty(dto.specialty());
        artist.setBio(dto.bio());

        TattooArtist updated = tattooArtistRepository.save(artist);
        return new TattooArtistResponseDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<BookedSlotResponseDTO> findBookedSlots(UUID artistId, LocalDateTime start, LocalDateTime end) {
        // Valida se o artista existe
        findEntityById(artistId);

        List<Appointment> appointments;
        if (start != null && end != null) {
            appointments = appointmentRepository.findByArtistIdAndSessionDateBetween(artistId, start, end);
        } else {
            appointments = appointmentRepository.findByArtistId(artistId);
        }

        return appointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .map(BookedSlotResponseDTO::new)
                .toList();
    }
}
