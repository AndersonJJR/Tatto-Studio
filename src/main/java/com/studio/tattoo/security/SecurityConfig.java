package com.studio.tattoo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/portfolio/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/artists/me").hasRole("ARTIST")
                        .requestMatchers(HttpMethod.PUT, "/artists/me").hasRole("ARTIST")
                        .requestMatchers(HttpMethod.GET, "/artists/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Endpoints restritos de portfólio para tatuadores
                        .requestMatchers(HttpMethod.POST, "/portfolio/**").hasRole("ARTIST")
                        .requestMatchers(HttpMethod.PUT, "/portfolio/**").hasRole("ARTIST")
                        .requestMatchers(HttpMethod.DELETE, "/portfolio/**").hasRole("ARTIST")

                        // Agendamentos exigem autenticação
                        .requestMatchers("/appointments/**").authenticated()

                        // Demais endpoints exigem autenticação
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.getWriter().write("""
                                    {
                                      "timestamp": "%s",
                                      "status": 401,
                                      "error": "Unauthorized",
                                      "message": "Acesso não autorizado. Token ausente, inválido ou expirado.",
                                      "path": "%s"
                                    }
                                    """.formatted(LocalDateTime.now(), request.getRequestURI()));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.getWriter().write("""
                                    {
                                      "timestamp": "%s",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Acesso negado. Você não possui permissão para acessar este recurso.",
                                      "path": "%s"
                                    }
                                    """.formatted(LocalDateTime.now(), request.getRequestURI()));
                        })
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
