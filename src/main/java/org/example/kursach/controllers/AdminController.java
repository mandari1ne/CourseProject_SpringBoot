package org.example.kursach.controllers;

import org.example.kursach.model.Role;
import org.example.kursach.model.Status;
import org.example.kursach.model.User;
import org.example.kursach.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

        String role = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(", "));
        model.addAttribute("role", role); // Передаем роль в модель

        return "admin/users"; // Возвращаем имя шаблона
    }

    @PostMapping("/update-user/{id}")
    public ResponseEntity<String> updateUserStatusAndRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> updates) {
        User user = userService.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        String newRole = updates.get("role");
        String newStatus = updates.get("status");

        user.setRole(Role.valueOf(newRole));
        user.setStatus(Status.valueOf(newStatus));

        userService.save(user);

        return ResponseEntity.ok("Изменения успешно сохранены!");
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.ok("Пользователь удален");
    }

}
