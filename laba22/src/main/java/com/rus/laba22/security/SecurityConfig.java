package com.rus.laba22.security;

import com.rus.laba22.repository.AppUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(AppUserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name())
                        .build())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenAuthenticationFilter tokenFilter) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/csrf").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("ADMIN")
                        .requestMatchers("/api/customers/**").hasRole("ADMIN")
                        .requestMatchers("/api/accounts/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/cards/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/transactions/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/operations/**").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(tokenFilter, BasicAuthenticationFilter.class);

        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

