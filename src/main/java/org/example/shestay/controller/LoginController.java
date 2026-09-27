package org.example.shestay.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shestay.entity.User;
import org.example.shestay.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        email = email == null
                ? ""
                : email.trim().toLowerCase();

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            model.addAttribute(
                    "error",
                    "Invalid email or password"
            );

            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute(
                    "error",
                    "Invalid email or password"
            );

            return "login";
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            model.addAttribute(
                    "error",
                    "Your account is inactive"
            );

            return "login";
        }

        // =========================
        // SAVE LOGIN SESSION
        // =========================

        session.setAttribute(
                "userId",
                user.getId()
        );

        session.setAttribute(
                "userName",
                user.getName()
        );

        session.setAttribute(
                "userRole",
                user.getRole()
        );

        // =========================
        // ROLE BASED REDIRECTION
        // =========================

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        }

        if ("OWNER".equals(user.getRole())) {
            return "redirect:/owner/dashboard";
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}