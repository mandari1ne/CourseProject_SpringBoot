package org.example.kursach.services;

import lombok.RequiredArgsConstructor;
import org.example.kursach.model.Status;
import org.example.kursach.model.User;
import org.example.kursach.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByLogin(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
//
//        // Добавляем префикс ROLE_ к роли из базы данных
//        String role = "ROLE_" + user.getRole().name();
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                user.getPassword(),
//                Collections.singletonList(new SimpleGrantedAuthority(role))
//        );
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        // Преобразуем статус в соответствующие флаги безопасности
        boolean isAccountNonLocked = user.getStatus() != Status.BLOCKED;
        boolean isEnabled = user.getStatus() == Status.ACTIVE;

        // Добавляем префикс ROLE_ к роли
        String role = "ROLE_" + user.getRole().name();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                isEnabled,
                true,
                true,
                isAccountNonLocked,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

}
