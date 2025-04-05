package org.example.kursach.repositories;

import org.example.kursach.model.VacationDays;
import org.example.kursach.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VacationDaysRepository extends JpaRepository<VacationDays, Long> {
    Optional<VacationDays> findByEmployee(User employee);
}
