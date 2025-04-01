package org.example.kursach.model;

import org.springframework.security.core.GrantedAuthority;

public enum Status implements GrantedAuthority {
    ACTIVE, BLOCKED;

    @Override
    public String getAuthority() {
        return name();
    }
}

