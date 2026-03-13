package com.rus.laba22.security;

import com.rus.laba22.model.AppUser;
import com.rus.laba22.repository.AppUserRepository;
import com.rus.laba22.service.AuthTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;
    private final AppUserRepository userRepository;

    public TokenAuthenticationFilter(AuthTokenService authTokenService, AppUserRepository userRepository) {
        this.authTokenService = authTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = authTokenService.getUsernameByToken(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AppUser user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

