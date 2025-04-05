package org.example.kursach.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "vacation_requests")
public class VacationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacationType vacationType; // Тип отпуска

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    // Метод для вычисления количества дней отпуска
    public long getVacationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;  // +1 чтобы включить первый день
        }
        return 0;
    }

    // Метод для проверки доступности отпуска по типу
    public boolean isVacationTypeAvailable(VacationDays vacationDays) {
        long requestedDays = getVacationDays();
        if (vacationType == VacationType.PAID) {
            return requestedDays <= vacationDays.getAvailablePaidDays();  // Проверка на оплачиваемый отпуск
        } else if (vacationType == VacationType.UNPAID) {
            return requestedDays <= vacationDays.getAvailableUnpaidDays();  // Проверка на неоплачиваемый отпуск
        }
        return false;
    }
}
