package com.jordipatuel.GrowTogetherAPI.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AuthUserDetails extends User {

    private final Long id;
    private final int tokenVersion;

    public AuthUserDetails(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, int tokenVersion) {
        super(username, password, authorities);
        this.id = id;
        this.tokenVersion = tokenVersion;
    }

    public Long getId() {
        return id;
    }

    public int getTokenVersion() {
        return tokenVersion;
    }
}
