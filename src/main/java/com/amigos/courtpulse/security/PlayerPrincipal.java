package com.amigos.courtpulse.security;

import com.amigos.courtpulse.entity.Player;
import com.amigos.courtpulse.enums.PlayerStatusEnum;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PlayerPrincipal implements UserDetails {

    private final Player player;

    public PlayerPrincipal(Player player) {
        this.player = player;
    }

    public Long getId() {
        return player.getId();
    }

    public String getPlayerCode() {
        return player.getPlayerCode();
    }

    public String getName() {
        return player.getName();
    }

    public String getGameName() {
        return player.getGameName();
    }

    public String getEmail() {
        return player.getEmail();
    }

    public boolean isEmailVerified() {
        return player.isEmailVerified();
    }

    public PlayerStatusEnum getStatus() {
        return player.getStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return player.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return player.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return PlayerStatusEnum.ACTIVE == player.getStatus();
    }
}
