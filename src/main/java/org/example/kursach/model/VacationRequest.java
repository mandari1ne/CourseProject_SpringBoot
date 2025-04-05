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
    private VacationStatus status = VacationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacationType vacationType;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    // Метод для вычисления количества дней отпуска
    public long getVacationDays() {
        if (startDate != null && endDate != null) {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
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

    // Конструктор по умолчанию для JPA
    public VacationRequest() {}

    // Конструктор с параметрами для удобства создания заявок
    public VacationRequest(User employee, LocalDate startDate, LocalDate endDate,
                           VacationStatus status, VacationType vacationType, User manager) {
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.vacationType = vacationType;
        this.manager = manager;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public VacationStatus getStatus() {
        return status;
    }

    public void setStatus(VacationStatus status) {
        this.status = status;
    }

    public VacationType getVacationType() {
        return vacationType;
    }

    public void setVacationType(VacationType vacationType) {
        this.vacationType = vacationType;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }
}
