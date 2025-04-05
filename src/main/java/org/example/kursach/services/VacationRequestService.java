package org.example.kursach.services;

import org.example.kursach.model.VacationRequest;
import org.example.kursach.model.User;
import org.example.kursach.repositories.VacationRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;

    public VacationRequestService(VacationRequestRepository vacationRequestRepository) {
        this.vacationRequestRepository = vacationRequestRepository;
    }

    // Сохранение заявки
    public VacationRequest save(VacationRequest vacationRequest) {
        return vacationRequestRepository.save(vacationRequest);
    }

    // Получение заявок по пользователю
    public List<VacationRequest> findByEmployee(User employee) {
        return vacationRequestRepository.findByEmployee(employee);
    }
}
