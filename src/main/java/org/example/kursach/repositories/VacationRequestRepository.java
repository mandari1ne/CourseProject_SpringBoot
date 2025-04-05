package org.example.kursach.repositories;

import org.example.kursach.model.VacationRequest;
import org.example.kursach.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacationRequestRepository extends JpaRepository<VacationRequest, Long> {
    List<VacationRequest> findByEmployee(User employee);
}
