package org.example.kursach.services;

import lombok.RequiredArgsConstructor;
import org.example.kursach.model.Status;
import org.example.kursach.model.User;
import org.example.kursach.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

import static org.example.kursach.model.Role.USER;
import static org.example.kursach.model.Status.ACTIVE;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        if (userRepository.findByLogin(user.getLogin()).isPresent()) {
            throw new IllegalArgumentException("❌ Пользователь с таким логином уже существует!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword())); // Хешируем пароль
        user.setRole(USER); // Устанавливаем роль по умолчанию
        user.setStatus(ACTIVE); // Устанавливаем статус по умолчанию
        return userRepository.save(user);
    }

    public Optional<User> findByLogin(String username) {
        return userRepository.findByLogin(username);
    }
}
