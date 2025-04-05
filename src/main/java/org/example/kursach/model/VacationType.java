package org.example.kursach.model;

import org.springframework.security.core.GrantedAuthority;

public enum VacationType implements GrantedAuthority {
    PAID, UNPAID;

    @Override
    public String getAuthority() {
        return name();
    }
}

