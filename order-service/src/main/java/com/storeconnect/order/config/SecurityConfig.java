package com.storeconnect.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final InternalHeaderAuthFilter internalHeaderAuthFilter;

    public SecurityConfig(InternalHeaderAuthFilter internalHeaderAuthFilter) {
        this.internalHeaderAuthFilter = internalHeaderAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Internal service-to-service endpoints
                .requestMatchers("/internal/**").permitAll()
                // Swagger & OpenAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Protected order checkout and management
                .anyRequest().authenticated()
            )
            .addFilterBefore(internalHeaderAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
