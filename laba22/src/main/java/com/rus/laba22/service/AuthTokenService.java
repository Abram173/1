package com.rus.laba22.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthTokenService {

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public String generateToken(String username) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, username);
        return token;
    }

    public String getUsernameByToken(String token) {
        return tokens.get(token);
    }
}

