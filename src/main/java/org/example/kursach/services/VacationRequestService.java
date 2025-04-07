package org.example.kursach.services;

import org.example.kursach.model.*;
import org.example.kursach.repositories.VacationDaysRepository;
import org.example.kursach.repositories.VacationRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VacationRequestService {

    private final VacationRequestRepository vacationRequestRepository;
    private final VacationDaysRepository vacationDaysRepository;

    public VacationRequestService(VacationRequestRepository vacationRequestRepository,
                                  VacationDaysRepository vacationDaysRepository) {
        this.vacationRequestRepository = vacationRequestRepository;
        this.vacationDaysRepository = vacationDaysRepository;
    }

    // Сохранение заявки
    public VacationRequest save(VacationRequest vacationRequest) {
        return vacationRequestRepository.save(vacationRequest);
    }

    // Получение заявок по пользователю
    public List<VacationRequest> findByEmployee(User employee) {
        return vacationRequestRepository.findByEmployee(employee);
    }

    public Optional<VacationRequest> findById(Long id) {
        return vacationRequestRepository.findById(id);
    }

    // Удаление заявки
    public void delete(VacationRequest vacationRequest) {
        vacationRequestRepository.delete(vacationRequest);
    }

    public List<VacationRequest> getRequestsByDepartment(String department) {
        return vacationRequestRepository.findAllByEmployeeDepartment(department);
    }

    // Одобрение заявки
    public void approveVacationRequest(Long requestId) {
        VacationRequest request = vacationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        if (request.getStatus() != VacationStatus.PENDING) {
            throw new RuntimeException("Заявка уже рассмотрена");
        }

        VacationDays vacationDays = vacationDaysRepository.findByEmployee(request.getEmployee())
                .orElseThrow(() -> new RuntimeException("Не найдены дни отпуска для пользователя"));

        int days = (int) request.getVacationDays();

        if (request.getVacationType() == VacationType.PAID) {
            if (vacationDays.getAvailablePaidDays() < days) {
                throw new RuntimeException("Недостаточно оплачиваемых дней отпуска");
            }
            vacationDays.usePaidVacationDays(days);
        } else if (request.getVacationType() == VacationType.UNPAID) {
            if (vacationDays.getAvailableUnpaidDays() < days) {
                throw new RuntimeException("Недостаточно неоплачиваемых дней отпуска");
            }
            vacationDays.useUnpaidVacationDays(days);
        }

        request.setStatus(VacationStatus.APPROVED);
        vacationDaysRepository.save(vacationDays);
        vacationRequestRepository.save(request);
    }

    // Отклонение заявки
    public void rejectVacationRequest(Long requestId) {
        VacationRequest request = vacationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        if (request.getStatus() != VacationStatus.PENDING) {
            throw new RuntimeException("Заявка уже рассмотрена");
        }

        request.setStatus(VacationStatus.REJECTED);
        vacationRequestRepository.save(request);
    }
}
