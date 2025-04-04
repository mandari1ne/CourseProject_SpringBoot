package org.example.kursach.services;

import lombok.RequiredArgsConstructor;
import org.example.kursach.model.Status;
import org.example.kursach.model.User;
import org.example.kursach.model.UserInfo;
import org.example.kursach.repositories.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<User> findAllUsersExcept(String login) {
        return userRepository.findByLoginNot(login);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void updateUser(String login, User updatedUser, UserInfo updatedUserInfo) {
        User existingUser = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        updatedUser.setLogin(existingUser.getLogin());

        if (existingUser.getUserInfo() == null) {
            existingUser.setUserInfo(new UserInfo());
        }

        // Получаем существующий объект UserInfo
        UserInfo existingUserInfo = existingUser.getUserInfo();

        // Устанавливаем все данные из updatedUserInfo
        existingUserInfo.setSurname(updatedUserInfo.getSurname());
        existingUserInfo.setName(updatedUserInfo.getName());
        existingUserInfo.setPatronymic(updatedUserInfo.getPatronymic());
        existingUserInfo.setDepartment(updatedUserInfo.getDepartment());
        existingUserInfo.setPosition(updatedUserInfo.getPosition());
        existingUserInfo.setEmail(updatedUserInfo.getEmail());

        existingUserInfo.setUser(existingUser);

        userRepository.save(existingUser);
    }

}
