package com.studio.tattoo.controller;

import com.studio.tattoo.model.user.User;
import com.studio.tattoo.model.user.UserService;
import com.studio.tattoo.model.user.dto.LoginRequestDTO;
import com.studio.tattoo.model.user.dto.RegisterRequestDTO;
import com.studio.tattoo.model.user.dto.TokenResponseDTO;
import com.studio.tattoo.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários com emissão de token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/login")
    @Operation(summary = "Realiza autenticação e emite token JWT")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        var authToken = new UsernamePasswordAuthenticationToken(dto.email().toLowerCase().trim(), dto.password());
        Authentication authentication = authenticationManager.authenticate(authToken);

        User user = (User) authentication.getPrincipal();
        String token = tokenService.generateToken(user);

        TokenResponseDTO response = new TokenResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário (Cliente ou Tatuador) e emite token JWT")
    public ResponseEntity<TokenResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        User registeredUser = userService.register(dto);
        String token = tokenService.generateToken(registeredUser);

        TokenResponseDTO response = new TokenResponseDTO(
                token,
                registeredUser.getId(),
                registeredUser.getName(),
                registeredUser.getEmail(),
                registeredUser.getRole().name()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
