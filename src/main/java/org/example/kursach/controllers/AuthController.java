package org.example.kursach.controllers;

import org.example.kursach.model.User;
import org.example.kursach.model.UserInfo;
import org.example.kursach.services.UserService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
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
    public String homePage() {
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


}
