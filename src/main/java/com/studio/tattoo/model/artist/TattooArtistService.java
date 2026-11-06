package com.studio.tattoo.model.artist;

import com.studio.tattoo.exception.BusinessRuleException;
import com.studio.tattoo.model.artist.dto.CreateTattooArtistDTO;
import com.studio.tattoo.model.artist.dto.TattooArtistResponseDTO;
import com.studio.tattoo.model.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TattooArtistService {

    private final TattooArtistRepository tattooArtistRepository;

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
    public TattooArtistResponseDTO findById(Long id) {
        return new TattooArtistResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public TattooArtist findEntityById(Long id) {
        return tattooArtistRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tatuador não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public TattooArtist findEntityByUserEmail(String email) {
        return tattooArtistRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Perfil de tatuador não encontrado para o e-mail: " + email));
    }

    @Transactional
    public TattooArtistResponseDTO update(Long id, CreateTattooArtistDTO dto, User authenticatedUser) {
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
}
