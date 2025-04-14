package org.example.kursach.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.kursach.model.User;
import org.example.kursach.model.VacationDays;
import org.example.kursach.model.VacationRequest;
import org.example.kursach.model.VacationStatus;
import org.example.kursach.services.UserService;
import org.example.kursach.services.VacationRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final VacationRequestService vacationRequestService;
    private final UserService userService;

    public ManagerController(VacationRequestService vacationRequestService, UserService userService) {
        this.vacationRequestService = vacationRequestService;
        this.userService = userService;
    }

    @GetMapping("/vacation-requests")
    public String viewDepartmentVacationRequests(Model model, Principal principal) {
        // Получаем текущего менеджера
        User manager = userService.findByLogin(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Получаем отдел менеджера
        String department = manager.getUserInfo().getDepartment();

        // Получаем все заявки сотрудников из этого отдела через сервис
        List<VacationRequest> vacationRequests = vacationRequestService.getRequestsByDepartment(department);

        model.addAttribute("vacationRequests", vacationRequests);
        return "manager/vacation-requests";
    }

    @PostMapping("/vacation-requests/{id}/approve")
    public String approveRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vacationRequestService.approveVacationRequest(id);
            redirectAttributes.addFlashAttribute("success", "Заявка успешно одобрена.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/manager/vacation-requests";
    }

    @PostMapping("/vacation-requests/{id}/reject")
    public String rejectVacationRequest(@PathVariable Long id) {
        vacationRequestService.rejectVacationRequest(id);
        return "redirect:/manager/vacation-requests";
    }

    @GetMapping("/vacation-requests/edit/{id}")
    public String editVacationRequest(@PathVariable Long id, Model model) {
        VacationRequest request = vacationRequestService.getById(id); // теперь метод существует
        if (request.getStatus() != VacationStatus.PENDING) {
            throw new RuntimeException("Редактировать можно только необработанные заявки.");
        }

        Long userId = request.getEmployee().getId();
        VacationDays vacationDays = vacationRequestService.getVacationDaysByUserId(userId);
        vacationDays.updateAvailableDays();

        model.addAttribute("request", request);
        model.addAttribute("vacationDays", vacationDays);

        return "manager/edit-vacation-request";
    }

    @PostMapping("/vacation-requests/edit/{id}")
    public String updateVacationRequest(
            @PathVariable Long id,
            VacationRequest updatedRequest,
            RedirectAttributes redirectAttributes) {
        try {
            vacationRequestService.updateVacationRequest(id, updatedRequest);
            redirectAttributes.addFlashAttribute("success", "Заявка обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/manager/vacation-requests";
    }

    @GetMapping("/vacation-report")
    public String vacationReport(Model model, Principal principal) {
        User manager = userService.findByLogin(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String department = manager.getUserInfo().getDepartment();

        List<User> employees = userService.getUsersByDepartment(department).stream()
                .filter(user -> !user.getId().equals(manager.getId()))
                .toList();

        List<Map<String, Object>> reportItems = employees.stream().map(user -> {
            VacationDays vacationDays = vacationRequestService.getVacationDaysByUserId(user.getId());
            vacationDays.updateAvailableDays();

            int used = vacationRequestService.getUsedVacationDays(user.getId());
            int total = vacationDays.getTotalDays();
            int availablePaid = vacationDays.getAvailablePaidDays();
            int availableUnpaid = vacationDays.getAvailableUnpaidDays();

            Map<String, Object> item = new HashMap<>();
            item.put("name", user.getUserInfo().getFullName());
            item.put("used", used);
            item.put("total", total);
            item.put("availablePaid", availablePaid);
            item.put("availableUnpaid", availableUnpaid);
            return item;
        }).toList();

        model.addAttribute("reportItems", reportItems);
        return "manager/vacation-report";
    }

    @GetMapping("/vacation-report/export")
    public void exportVacationReportToExcel(HttpServletResponse response, Principal principal) throws Exception {
        User manager = userService.findByLogin(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String department = manager.getUserInfo().getDepartment();

        List<User> employees = userService.getUsersByDepartment(department).stream()
                .filter(user -> !user.getId().equals(manager.getId()))
                .toList();

        // Установка параметров ответа
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=vacation_report.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчет по отпускам");

            // Заголовки
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Сотрудник", "Общее кол-во дней", "Использовано", "Доступно (оплач./неопл.)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Заполнение данными
            int rowNum = 1;
            for (User user : employees) {
                VacationDays vacationDays = vacationRequestService.getVacationDaysByUserId(user.getId());
                vacationDays.updateAvailableDays();

                int used = vacationRequestService.getUsedVacationDays(user.getId());
                int total = vacationDays.getTotalDays();
                int availablePaid = vacationDays.getAvailablePaidDays();
                int availableUnpaid = vacationDays.getAvailableUnpaidDays();

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getUserInfo().getFullName());
                row.createCell(1).setCellValue(total);
                row.createCell(2).setCellValue(used);
                row.createCell(3).setCellValue(availablePaid + " / " + availableUnpaid);
            }

            // Автоширина
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

}
