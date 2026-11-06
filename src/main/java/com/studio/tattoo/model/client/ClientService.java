package com.studio.tattoo.model.client;

import com.studio.tattoo.model.client.dto.ClientResponseDTO;
import com.studio.tattoo.model.client.dto.CreateClientDTO;
import com.studio.tattoo.model.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public ClientResponseDTO getProfile(User authenticatedUser) {
        Client client = findEntityByUserEmail(authenticatedUser.getEmail());
        return new ClientResponseDTO(client);
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO findById(Long id) {
        return new ClientResponseDTO(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Client findEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public Client findEntityByUserEmail(String email) {
        return clientRepository.findByUserEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Perfil de cliente não encontrado para o e-mail: " + email));
    }

    @Transactional
    public ClientResponseDTO update(CreateClientDTO dto, User authenticatedUser) {
        Client client = findEntityByUserEmail(authenticatedUser.getEmail());

        client.setName(dto.name());
        client.setPhone(dto.phone());
        client.setPreferences(dto.preferences());

        Client updated = clientRepository.save(client);
        return new ClientResponseDTO(updated);
    }
}
