package org.example.kursach.controllers;

import org.example.kursach.model.Role;
import org.example.kursach.model.Status;
import org.example.kursach.model.User;
import org.example.kursach.model.UserInfo;
import org.example.kursach.repositories.UserInfoRepository;
import org.example.kursach.repositories.UserRepository;
import org.example.kursach.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AdminController(UserService userService, UserRepository userRepository, UserInfoRepository userInfoRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userInfoRepository = userInfoRepository;
        this.passwordEncoder = passwordEncoder;
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

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("userInfo", new UserInfo());
        return "admin/register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String login,
                               @RequestParam String password,
                               @RequestParam String role,
                               @RequestParam String surname,
                               @RequestParam String name,
                               @RequestParam(required = false) String patronymic,
                               @RequestParam String department,
                               @RequestParam String position,
                               @RequestParam String email,
                               Model model) {

        if (login.length() < 3) {
            model.addAttribute("loginError", "Логин должен быть не менее 3 символов");
        } else if (password.length() < 6) {
            model.addAttribute("passwordError", "Пароль должен быть не менее 6 символов");
        } else if (userRepository.findByLogin(login).isPresent()) {
            model.addAttribute("loginError", "Логин уже существует");
        }

        if (surname.isEmpty() || name.isEmpty()) {
            model.addAttribute("fieldError", "Фамилия и имя обязательны для заполнения");
        }

        if (model.containsAttribute("loginError") || model.containsAttribute("passwordError") || model.containsAttribute("fieldError")) {
            model.addAttribute("user", new User());
            model.addAttribute("userInfo", new UserInfo());
            return "admin/register";
        }

        User user = new User();
        user.setLogin(login);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.valueOf(role));
        user.setStatus(Status.ACTIVE);

        UserInfo userInfo = new UserInfo();
        userInfo.setSurname(surname);
        userInfo.setName(name);
        userInfo.setPatronymic(patronymic);
        userInfo.setDepartment(department);
        userInfo.setPosition(position);
        userInfo.setEmail(email);

        user.setUserInfo(userInfo);
        userInfo.setUser(user);

        userRepository.save(user);

        return "redirect:/admin/users";
    }

}
