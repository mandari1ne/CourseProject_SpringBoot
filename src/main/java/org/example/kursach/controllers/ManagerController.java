package org.example.kursach.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.kursach.model.*;
import org.example.kursach.services.UserService;
import org.example.kursach.services.VacationRequestService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

//    @GetMapping("/vacation-requests")
//    public String viewDepartmentVacationRequests(Model model, Principal principal) {
//        // Получаем текущего менеджера
//        User manager = userService.findByLogin(principal.getName())
//                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
//
//        // Получаем отдел менеджера
//        String department = manager.getUserInfo().getDepartment();
//
//        // Получаем все заявки сотрудников из этого отдела через сервис
//        List<VacationRequest> vacationRequests = vacationRequestService.getRequestsByDepartment(department);
//
//        model.addAttribute("vacationRequests", vacationRequests);
//        return "manager/vacation-requests";
//    }

    @GetMapping("/vacation-requests")
    public String viewVacationRequests(Model model, Principal principal) {
        // Получаем текущего пользователя
        User currentUser = userService.findByLogin(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<VacationRequest> vacationRequests;

        // Если администратор — показываем все заявки
        if (currentUser.getRole() == Role.ADMIN) {
            vacationRequests = vacationRequestService.getAllRequests();
            model.addAttribute("isAdmin", true);
        } else if (currentUser.getRole() == Role.MANAGER) {
            // Если менеджер — только по отделу
            String department = currentUser.getUserInfo().getDepartment();
            vacationRequests = vacationRequestService.getRequestsByDepartment(department);
            model.addAttribute("isAdmin", false);
        } else {
            throw new AccessDeniedException("У вас нет прав для просмотра заявок");
        }

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
        User currentUser = userService.findByLogin(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<User> employees;

        if (currentUser.getRole() == Role.ADMIN) {
            // Админ — все пользователи, кроме себя
            employees = userService.findAllUsersExcept(currentUser.getLogin());
            model.addAttribute("isAdmin", true);
        } else if (currentUser.getRole() == Role.MANAGER) {
            // Менеджер — только сотрудники своего отдела, кроме себя
            String department = currentUser.getUserInfo().getDepartment();
            employees = userService.getUsersByDepartment(department).stream()
                    .filter(user -> !user.getId().equals(currentUser.getId()))
                    .toList();
            model.addAttribute("isAdmin", false);
        } else {
            throw new AccessDeniedException("У вас нет прав для просмотра отчета");
        }

        // Формируем отчёт
        List<Map<String, Object>> reportItems = employees.stream().map(user -> {
            VacationDays vacationDays = vacationRequestService.getVacationDaysByUserId(user.getId());
            vacationDays.updateAvailableDays();

            int used = vacationRequestService.getUsedVacationDays(user.getId());
            int total = vacationDays.getTotalDays();
            int availablePaid = vacationDays.getAvailablePaidDays();
            int availableUnpaid = vacationDays.getAvailableUnpaidDays();

            Map<String, Object> item = new HashMap<>();
            item.put("name", user.getUserInfo().getFullName());
            item.put("department", user.getUserInfo().getDepartment());
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
    public void exportVacationReportToExcel(@RequestParam(required = false) String departmentFilter,
                                            HttpServletResponse response,
                                            Principal principal) throws Exception {
        User currentUser = userService.findByLogin(principal.getName())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<User> employees;

        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (isAdmin) {
            employees = userService.findAllUsersExcept(currentUser.getLogin());
            if (departmentFilter != null && !departmentFilter.equalsIgnoreCase("all")) {
                String filter = departmentFilter.toLowerCase();
                employees = employees.stream()
                        .filter(user -> user.getUserInfo().getDepartment().toLowerCase().contains(filter))
                        .toList();
            }
        } else if (currentUser.getRole() == Role.MANAGER) {
            String department = currentUser.getUserInfo().getDepartment();
            employees = userService.getUsersByDepartment(department).stream()
                    .filter(user -> !user.getId().equals(currentUser.getId()))
                    .toList();
        } else {
            throw new AccessDeniedException("Нет доступа к отчету");
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=vacation_report.xlsx");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Отчет по отпускам");

            Row headerRow = sheet.createRow(0);
            int col = 0;

            headerRow.createCell(col++).setCellValue("Сотрудник");
            if (isAdmin) {
                headerRow.createCell(col++).setCellValue("Отдел");
            }
            headerRow.createCell(col++).setCellValue("Общее кол-во дней");
            headerRow.createCell(col++).setCellValue("Использовано");
            headerRow.createCell(col).setCellValue("Доступно (оплач./неопл.)");

            int rowNum = 1;
            for (User user : employees) {
                VacationDays vacationDays = vacationRequestService.getVacationDaysByUserId(user.getId());
                vacationDays.updateAvailableDays();

                int used = vacationRequestService.getUsedVacationDays(user.getId());
                int total = vacationDays.getTotalDays();
                int availablePaid = vacationDays.getAvailablePaidDays();
                int availableUnpaid = vacationDays.getAvailableUnpaidDays();

                Row row = sheet.createRow(rowNum++);
                int columnIndex = 0;

                row.createCell(columnIndex++).setCellValue(user.getUserInfo().getFullName());
                if (isAdmin) {
                    row.createCell(columnIndex++).setCellValue(user.getUserInfo().getDepartment());
                }
                row.createCell(columnIndex++).setCellValue(total);
                row.createCell(columnIndex++).setCellValue(used);
                row.createCell(columnIndex).setCellValue(availablePaid + " / " + availableUnpaid);
            }

            // Автоширина
            for (int i = 0; i <= (isAdmin ? 4 : 3); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }

}
