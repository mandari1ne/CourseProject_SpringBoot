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

    private int totalDays = 50;  // Общее количество дней отпуска (50)

    private int paidDays = 30;   // Количество оплачиваемых дней отпуска (30)
    private int unpaidDays = 20; // Количество неоплачиваемых дней отпуска (20)

    private int usedPaidDays = 0;   // Использованные оплачиваемые дни
    private int usedUnpaidDays = 0; // Использованные неоплачиваемые дни

    private int availablePaidDays;  // Доступные оплачиваемые дни
    private int availableUnpaidDays; // Доступные неоплачиваемые дни

    // Метод для обновления доступных дней отпуска
    public void updateAvailableDays() {
        this.availablePaidDays = paidDays - usedPaidDays;
        this.availableUnpaidDays = unpaidDays - usedUnpaidDays;
    }

    // Метод для добавления использованных оплачиваемых дней
    public void usePaidVacationDays(int days) {
        if (days <= availablePaidDays) {
            this.usedPaidDays += days;
            updateAvailableDays();
        }
    }

    // Метод для добавления использованных неоплачиваемых дней
    public void useUnpaidVacationDays(int days) {
        if (days <= availableUnpaidDays) {
            this.usedUnpaidDays += days;
            updateAvailableDays();
        }
    }

    // Метод для получения доступных оплачиваемых дней
    public int getAvailablePaidDays() {
        return availablePaidDays;
    }

    // Метод для получения доступных неоплачиваемых дней
    public int getAvailableUnpaidDays() {
        return availableUnpaidDays;
    }

    // Геттеры и сеттеры для полей
    public int getPaidDays() {
        return paidDays;
    }

    public void setPaidDays(int paidDays) {
        this.paidDays = paidDays;
        updateAvailableDays();
    }

    public int getUnpaidDays() {
        return unpaidDays;
    }

    public void setUnpaidDays(int unpaidDays) {
        this.unpaidDays = unpaidDays;
        updateAvailableDays();
    }

    public int getUsedPaidDays() {
        return usedPaidDays;
    }

    public void setUsedPaidDays(int usedPaidDays) {
        this.usedPaidDays = usedPaidDays;
        updateAvailableDays();
    }

    public int getUsedUnpaidDays() {
        return usedUnpaidDays;
    }

    public void setUsedUnpaidDays(int usedUnpaidDays) {
        this.usedUnpaidDays = usedUnpaidDays;
        updateAvailableDays();
    }
}
