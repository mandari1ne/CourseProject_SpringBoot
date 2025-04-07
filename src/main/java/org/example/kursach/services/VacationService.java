package org.example.kursach.services;

import lombok.RequiredArgsConstructor;
import org.example.kursach.model.*;
import org.example.kursach.repositories.VacationDaysRepository;
import org.example.kursach.repositories.VacationRequestRepository;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class VacationService {

    private final VacationDaysRepository vacationDaysRepository;
    private final VacationRequestRepository vacationRequestRepository;

    public VacationDays getVacationDays(User user) {
        return vacationDaysRepository.findByEmployee(user)
                .orElseThrow(() -> new RuntimeException("Не найдены данные об отпусках для пользователя: " + user.getLogin()));
    }

    public void submitRequest(User user, VacationRequest vacationRequest) {
        validateVacationRequest(user, vacationRequest);
        vacationRequest.setEmployee(user);
        vacationRequest.setStatus(VacationStatus.PENDING); // если у тебя есть поле статус
        vacationRequestRepository.save(vacationRequest);
    }

    private void validateVacationRequest(User user, VacationRequest request) {
        VacationDays days = getVacationDays(user);

        long daysRequested = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("❌ Дата начала не может быть позже даты окончания.");
        }

        if (request.getVacationType() == VacationType.PAID && daysRequested > days.getAvailablePaidDays()) {
            throw new IllegalArgumentException("❌ У вас недостаточно оплачиваемых дней отпуска.");
        }

        if (request.getVacationType() == VacationType.UNPAID && daysRequested > days.getAvailableUnpaidDays()) {
            throw new IllegalArgumentException("❌ У вас недостаточно неоплачиваемых дней отпуска.");
        }
    }

    public void updateVacationRequest(User user, VacationRequest updatedRequest) {
        VacationRequest existingRequest = vacationRequestRepository.findById(updatedRequest.getId())
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        if (!existingRequest.getEmployee().getId().equals(user.getId())) {
            throw new RuntimeException("У вас нет прав на редактирование этой заявки");
        }

        if (existingRequest.getStatus() != VacationStatus.PENDING) {
            throw new RuntimeException("Можно редактировать только заявки со статусом 'PENDING'");
        }

        // Валидация нового запроса
        updatedRequest.setEmployee(user); // для корректной валидации
        validateVacationRequest(user, updatedRequest);

        // Обновление данных
        existingRequest.setStartDate(updatedRequest.getStartDate());
        existingRequest.setEndDate(updatedRequest.getEndDate());
        existingRequest.setVacationType(updatedRequest.getVacationType());

        vacationRequestRepository.save(existingRequest);
    }

}
