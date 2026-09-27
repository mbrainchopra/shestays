package org.example.shestay.controller;

import org.example.shestay.entity.User;
import org.example.shestay.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================================================
    // USER REGISTRATION PAGE
    // =========================================================

    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        model.addAttribute(
                "user",
                new User()
        );

        return "register";
    }

    // =========================================================
    // USER REGISTRATION
    // =========================================================

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") User user,
            Model model) {

        String name = user.getName() == null
                ? ""
                : user.getName().trim();

        String email = user.getEmail() == null
                ? ""
                : user.getEmail().trim().toLowerCase();

        String password = user.getPassword() == null
                ? ""
                : user.getPassword();

        // -----------------------------------------------------
        // VALIDATION
        // -----------------------------------------------------

        if (name.isBlank()
                || email.isBlank()
                || password.isBlank()) {

            model.addAttribute(
                    "error",
                    "Please enter your name, email and password."
            );

            return "register";
        }

        if (password.length() < 6) {

            model.addAttribute(
                    "error",
                    "Password must be at least 6 characters."
            );

            return "register";
        }

        // -----------------------------------------------------
        // DUPLICATE EMAIL CHECK
        // -----------------------------------------------------

        if (userRepository.existsByEmail(email)) {

            model.addAttribute(
                    "error",
                    "Email already registered."
            );

            return "register";
        }

        // -----------------------------------------------------
        // SAVE USER
        // -----------------------------------------------------

        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");
        user.setStatus("ACTIVE");

        userRepository.save(user);

        return "redirect:/login";
    }
}