package org.example.kursach.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ADMIN, MANAGER, USER, BLOCKED;

    @Override
    public String getAuthority() {
        return name();
    }
}
