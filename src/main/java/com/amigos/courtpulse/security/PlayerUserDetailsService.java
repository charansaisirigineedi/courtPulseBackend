package com.amigos.courtpulse.security;

import com.amigos.courtpulse.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerUserDetailsService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return playerRepository.findByUsername(username.trim())
                .map(PlayerPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
    }
}
