package org.example.kursach.controllers;

import org.example.kursach.model.User;
import org.example.kursach.model.VacationRequest;
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
import java.util.List;

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
}
