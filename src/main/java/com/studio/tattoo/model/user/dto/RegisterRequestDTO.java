package com.studio.tattoo.model.user.dto;

import com.studio.tattoo.model.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100, message = "O nome deve conter entre 2 e 100 caracteres")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve conter no mínimo 6 caracteres")
        String password,

        @NotNull(message = "O perfil de acesso (role) é obrigatório: ROLE_CLIENT ou ROLE_ARTIST")
        Role role,

        String phone,
        String specialty,
        String bio,
        String preferences
) {}
