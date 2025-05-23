package org.example.kursach.repositories;

import org.example.kursach.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
    List<User> findByLoginNot(String login);
    List<User> findByUserInfo_Department(String department);
}
