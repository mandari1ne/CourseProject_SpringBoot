package org.example.kursach.controllers;

import org.example.kursach.model.User;
import org.example.kursach.services.UserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        List<User> users = userService.findAllUsersExcept(currentUser.getUsername());
        model.addAttribute("users", users);

        // Получаем роль текущего пользователя и передаем её в модель
        String role = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        model.addAttribute("role", role); // Передаем роль в модель

        return "admin/users"; // Возвращаем имя шаблона
    }
}
