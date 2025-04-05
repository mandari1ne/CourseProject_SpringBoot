package org.example.kursach.services;

import org.example.kursach.model.VacationDays;
import org.example.kursach.model.User;
import org.example.kursach.repositories.VacationDaysRepository;
import org.springframework.stereotype.Service;

@Service
public class VacationDaysService {

    private final VacationDaysRepository vacationDaysRepository;

    public VacationDaysService(VacationDaysRepository vacationDaysRepository) {
        this.vacationDaysRepository = vacationDaysRepository;
    }

    public VacationDays getVacationDaysByUser(User user) {
        // Пытаемся найти запись о днях отпуска для пользователя
        return vacationDaysRepository.findByEmployee(user)
                .orElseGet(() -> createDefaultVacationDays(user));  // Если не найдено, создаем дефолтные значения
    }

    private VacationDays createDefaultVacationDays(User user) {
        // Создаем дефолтную запись о днях отпуска
        VacationDays vacationDays = new VacationDays();
        vacationDays.setEmployee(user);
        vacationDays.setPaidDays(30);  // Примерное количество оплачиваемых дней отпуска
        vacationDays.setUnpaidDays(20);  // Примерное количество неоплачиваемых дней отпуска
        vacationDays.setTotalDays(50);  // Примерное общее количество дней отпуска

        vacationDays.updateAvailableDays();  // Обновляем доступные дни

        // Сохраняем в базе данных
        return vacationDaysRepository.save(vacationDays);
    }
}
