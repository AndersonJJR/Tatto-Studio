package com.studio.tattoo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studio.tattoo.model.user.Role;
import com.studio.tattoo.model.user.User;
import com.studio.tattoo.model.user.UserService;
import com.studio.tattoo.model.user.dto.RegisterRequestDTO;
import com.studio.tattoo.security.SecurityConfig;
import com.studio.tattoo.security.SecurityFilter;
import com.studio.tattoo.security.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private SecurityFilter securityFilter;

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso e retornar 201 Created")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Marina Silva",
                "marina@gmail.com",
                "senha123",
                Role.ROLE_CLIENT,
                "11988887777",
                null,
                null,
                "Aquarela"
        );

        User savedUser = User.builder()
                .id(1L)
                .name("Marina Silva")
                .email("marina@gmail.com")
                .role(Role.ROLE_CLIENT)
                .build();

        when(userService.register(any(RegisterRequestDTO.class))).thenReturn(savedUser);
        when(tokenService.generateToken(any(User.class))).thenReturn("jwt-token-simulado");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-simulado"))
                .andExpect(jsonPath("$.email").value("marina@gmail.com"))
                .andExpect(jsonPath("$.role").value("ROLE_CLIENT"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao tentar registrar usuário com e-mail inválido ou campos em branco")
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        RegisterRequestDTO invalidDto = new RegisterRequestDTO(
                "", // nome vazio
                "email-invalido", // formato inválido
                "123", // senha menor que 6 chars
                null, // role nula
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists())
                .andExpect(jsonPath("$.validationErrors.role").exists());
    }
}
