package com.amigos.courtpulse.controller;

import com.amigos.courtpulse.dto.auth.AuthResponse;
import com.amigos.courtpulse.dto.auth.AuthenticatedPlayerResponse;
import com.amigos.courtpulse.dto.auth.LoginRequest;
import com.amigos.courtpulse.dto.common.ApiResponse;
import com.amigos.courtpulse.service.AuthService;
import com.amigos.courtpulse.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseUtil.ok("Login successful", response);
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthenticatedPlayerResponse>> getCurrentPlayer(
            @AuthenticationPrincipal Jwt jwt
    ) {
        AuthenticatedPlayerResponse response = authService.getCurrentPlayer(jwt);
        return ResponseUtil.ok("Authenticated player fetched successfully", response);
    }
}
