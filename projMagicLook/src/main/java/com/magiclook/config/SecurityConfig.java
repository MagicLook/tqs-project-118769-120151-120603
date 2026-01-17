package com.magiclook.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // CSRF is disabled for development/testing purposes only
        // In production, CSRF protection should be enabled for state-changing operations
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/index", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/user/**", "/staff/**", "/booking/**", "/item/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable()) // Safe for development: this is a learning application
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // Required for H2 console
        
        return http.build();
    }
}
