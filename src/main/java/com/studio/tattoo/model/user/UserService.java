package com.studio.tattoo.model.user;

import com.studio.tattoo.exception.BusinessRuleException;
import com.studio.tattoo.model.artist.TattooArtist;
import com.studio.tattoo.model.artist.TattooArtistRepository;
import com.studio.tattoo.model.client.Client;
import com.studio.tattoo.model.client.ClientRepository;
import com.studio.tattoo.model.user.dto.RegisterRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TattooArtistRepository tattooArtistRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    @Transactional
    public User register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessRuleException("O e-mail informado já se encontra cadastrado no sistema.");
        }

        User user = User.builder()
                .name(dto.name())
                .email(dto.email().toLowerCase().trim())
                .password(passwordEncoder.encode(dto.password()))
                .role(dto.role())
                .build();

        User savedUser = userRepository.save(user);

        if (dto.role() == Role.ROLE_ARTIST) {
            TattooArtist artist = TattooArtist.builder()
                    .user(savedUser)
                    .name(dto.name())
                    .phone(dto.phone())
                    .specialty(dto.specialty())
                    .bio(dto.bio())
                    .build();
            tattooArtistRepository.save(artist);
        } else if (dto.role() == Role.ROLE_CLIENT) {
            Client client = Client.builder()
                    .user(savedUser)
                    .name(dto.name())
                    .phone(dto.phone())
                    .preferences(dto.preferences())
                    .build();
            clientRepository.save(client);
        }

        return savedUser;
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com e-mail: " + email));
    }
}
