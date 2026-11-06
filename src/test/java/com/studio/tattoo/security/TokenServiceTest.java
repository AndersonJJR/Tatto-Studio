package com.studio.tattoo.security;

import com.studio.tattoo.model.user.Role;
import com.studio.tattoo.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        // Chave HMAC-SHA de pelo menos 256 bits para o JJWT
        ReflectionTestUtils.setField(tokenService, "secret", "super-secret-key-for-testing-purposes-1234567890-must-be-long-enough");
        ReflectionTestUtils.setField(tokenService, "expirationHours", 2L);
    }

    @Test
    @DisplayName("Deve gerar um token JWT válido para um usuário com JJWT")
    void shouldGenerateValidToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .name("Carlos Tatuador")
                .email("carlos@studio.com")
                .role(Role.ROLE_ARTIST)
                .build();

        String token = tokenService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String subject = tokenService.validateToken(token);
        assertEquals("carlos@studio.com", subject);
    }

    @Test
    @DisplayName("Deve retornar nulo ao validar um token adulterado ou inválido")
    void shouldReturnNullForInvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalidpayload.invalidsignature";
        String subject = tokenService.validateToken(invalidToken);

        assertNull(subject);
    }
}
