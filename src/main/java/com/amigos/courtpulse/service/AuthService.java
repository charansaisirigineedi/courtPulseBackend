package com.amigos.courtpulse.service;

import com.amigos.courtpulse.dto.auth.AuthResponse;
import com.amigos.courtpulse.dto.auth.AuthenticatedPlayerResponse;
import com.amigos.courtpulse.dto.auth.LoginRequest;
import org.springframework.security.oauth2.jwt.Jwt;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthenticatedPlayerResponse getCurrentPlayer(Jwt jwt);
}
