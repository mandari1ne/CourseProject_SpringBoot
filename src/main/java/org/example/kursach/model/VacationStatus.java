package org.example.kursach.model;

import org.springframework.security.core.GrantedAuthority;

public enum VacationStatus implements GrantedAuthority {
    PENDING, APPROVED, REJECTED;

    @Override
    public String getAuthority() {
        return name();
    }
}

