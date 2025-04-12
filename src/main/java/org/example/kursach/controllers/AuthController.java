package org.example.kursach.controllers;

import org.example.kursach.model.*;
import org.example.kursach.services.UserService;
import org.example.kursach.services.VacationDaysService;
import org.example.kursach.services.VacationRequestService;
import org.example.kursach.services.VacationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final VacationRequestService vacationRequestService;
    private final VacationDaysService vacationDaysService;
    private final VacationService vacationService;

    public AuthController(UserService userService,
                          VacationRequestService vacationRequestService,
                          VacationDaysService vacationDaysService,
                          VacationService vacationService) {
        this.userService = userService;
        this.vacationRequestService = vacationRequestService;
        this.vacationDaysService = vacationDaysService;
        this.vacationService = vacationService;
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        if (userService.findByLogin(user.getLogin()).isPresent()) {
            model.addAttribute("error", "❌ Пользователь с таким логином уже существует!");
            return "register";
        }
        userService.registerUser(user);
        return "redirect:/auth/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login() {
        return "redirect:/auth/home";
    }

    @GetMapping("/home")
    public String homePage(Model model, Principal principal) {
        if (principal != null) {
            String login = principal.getName();
            User user = userService.findByLogin(login)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

            model.addAttribute("role", user.getRole().name());
        }
        return "home";
    }

    @GetMapping("/profile")
    public String showAccountPage(Model model, Principal principal) {
        String login = principal.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        if (user.getUserInfo() == null) {
            user.setUserInfo(new UserInfo());
        }

        model.addAttribute("user", user);
        model.addAttribute("userInfo", user.getUserInfo());

        return "profile";
    }

    @GetMapping("/edit-profile")
    public String showEditProfileForm(Model model, Principal principal) {
        String login = principal.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        if (user.getUserInfo() == null) {
            user.setUserInfo(new UserInfo());
        }

        model.addAttribute("user", user);
        model.addAttribute("userInfo", user.getUserInfo());

        return "edit-profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute User updatedUser, @ModelAttribute UserInfo updatedUserInfo, Principal principal) {
        userService.updateUser(principal.getName(), updatedUser, updatedUserInfo);
        return "redirect:/auth/profile";
    }

    // Страница с заявками на отпуск
    @GetMapping("/vacation-requests")
    public String showVacationRequests(Model model, Principal principal) {
        String login = principal.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        List<VacationRequest> vacationRequests = vacationRequestService.findByEmployee(user);
        model.addAttribute("vacationRequests", vacationRequests);

        return "vacation-requests";  // Отображаем шаблон для списка заявок
    }

    // Страница для подачи новой заявки на отпуск
    @GetMapping("/vacation-request")
    public String showVacationRequestForm(Model model, Principal principal) {
        String login = principal.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        // Получаем информацию о днях отпуска, если она не найдена, то создаем дефолтные данные
        VacationDays vacationDays = vacationDaysService.getVacationDaysByUser(user);
        model.addAttribute("vacationDays", vacationDays);
        model.addAttribute("vacationRequest", new VacationRequest());

        return "vacation-request";  // Отображаем шаблон для создания заявки
    }


    // Обработка отправки заявки на отпуск
    @PostMapping("/vacation-request")
    public String submitVacationRequest(@ModelAttribute VacationRequest vacationRequest,
                                        @AuthenticationPrincipal UserDetails userDetails,
                                        Model model) {
        User user = userService.findByLogin(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        try {
            vacationService.submitRequest(user, vacationRequest);
            return "redirect:/auth/vacation-requests";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vacationRequest", vacationRequest);
            model.addAttribute("vacationDays", vacationService.getVacationDays(user));
            return "vacation-request";
        }
    }

    // Удаление заявки на отпуск
    @PostMapping("/vacation-request/delete/{id}")
    public String deleteVacationRequest(@PathVariable Long id, Principal principal, Model model) {
        String login = principal.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        Optional<VacationRequest> vacationRequestOpt = vacationRequestService.findById(id);

        if (vacationRequestOpt.isPresent()) {
            VacationRequest vacationRequest = vacationRequestOpt.get();

            // Проверка статуса заявки
            if (vacationRequest.getStatus() == VacationStatus.PENDING) {
                vacationRequestService.delete(vacationRequest);
                model.addAttribute("success", "✅ Заявка на отпуск успешно удалена!");
            } else {
                model.addAttribute("error", "❌ Вы не можете удалить заявку, так как ее статус не 'Ожидает'.");
            }
        } else {
            model.addAttribute("error", "❌ Заявка не найдена.");
        }

        // Перенаправление на страницу с заявками
        List<VacationRequest> vacationRequests = vacationRequestService.findByEmployee(user);
        model.addAttribute("vacationRequests", vacationRequests);
        return "vacation-requests";
    }

    @GetMapping("/vacation-request/edit/{id}")
    public String showEditVacationRequestForm(@PathVariable Long id, Model model, Principal principal) {
        String login = principal.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        VacationRequest request = vacationRequestService.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        if (!request.getEmployee().getId().equals(user.getId())) {
            throw new RuntimeException("Вы не можете редактировать чужую заявку");
        }

        if (request.getStatus() != VacationStatus.PENDING) {
            throw new RuntimeException("Можно редактировать только заявки со статусом 'Ожидает'");
        }

        VacationDays vacationDays = vacationDaysService.getVacationDaysByUser(user);

        model.addAttribute("vacationRequest", request);
        model.addAttribute("vacationDays", vacationDays);

        return "edit-vacation-request";
    }

    @PostMapping("/vacation-request/edit/{id}")
    public String updateVacationRequest(@PathVariable Long id,
                                        @ModelAttribute VacationRequest updatedRequest,
                                        Principal principal,
                                        Model model) {
        String login = principal.getName();
        User user = userService.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + login));

        try {
            updatedRequest.setId(id);
            vacationService.updateVacationRequest(user, updatedRequest);
            return "redirect:/auth/vacation-requests";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vacationRequest", updatedRequest);
            model.addAttribute("vacationDays", vacationService.getVacationDays(user));
            return "edit-vacation-request";
        }
    }

}
