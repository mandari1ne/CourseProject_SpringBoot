package org.example.kursach.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vacation_days")
public class VacationDays {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User employee;

    private int totalDays;
    private int usedDays;
    private int availableDays;

    // Метод для обновления оставшихся дней отпуска
    public void updateAvailableDays() {
        this.availableDays = totalDays - usedDays;
    }

    // Метод для добавления использованных дней отпуска
    public void useVacationDays(int days) {
        if (days <= availableDays) {
            this.usedDays += days;
            updateAvailableDays();
        }
    }

    // Метод для возврата доступных дней
    public int getAvailableDays() {
        return availableDays;
    }

}
