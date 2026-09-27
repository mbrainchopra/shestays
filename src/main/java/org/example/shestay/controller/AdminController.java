package org.example.shestay.controller;

import jakarta.servlet.http.HttpSession;
import org.example.shestay.entity.Booking;
import org.example.shestay.entity.Property;
import org.example.shestay.entity.Room;
import org.example.shestay.entity.User;
import org.example.shestay.repository.BookingRepository;
import org.example.shestay.repository.PropertyRepository;
import org.example.shestay.repository.RoomRepository;
import org.example.shestay.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AdminController(
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {

        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }


    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        long totalUsers =
                userRepository.countByRole("USER");

        long totalOwners =
                userRepository.countByRole("OWNER");

        long totalProperties =
                propertyRepository.count();

        long totalRooms =
                roomRepository.count();

        long totalBookings =
                bookingRepository.count();

        long pendingBookings =
                bookingRepository.countByStatus("REQUESTED");

        model.addAttribute(
                "totalUsers",
                totalUsers
        );

        model.addAttribute(
                "totalOwners",
                totalOwners
        );

        model.addAttribute(
                "totalProperties",
                totalProperties
        );

        model.addAttribute(
                "totalRooms",
                totalRooms
        );

        model.addAttribute(
                "totalBookings",
                totalBookings
        );

        model.addAttribute(
                "pendingBookings",
                pendingBookings
        );

        model.addAttribute(
                "adminName",
                session.getAttribute("userName")
        );

        return "admin/dashboard";
    }


    // =====================================================
    // MANAGE USERS
    // =====================================================

    @GetMapping("/users")
    public String manageUsers(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users = userRepository.findAll()
                .stream()
                .filter(user ->
                        "USER".equals(user.getRole()))
                .toList();

        model.addAttribute(
                "users",
                users
        );

        return "admin/users";
    }


    // =====================================================
    // ACTIVATE USER
    // =====================================================

    @PostMapping("/user/{id}/activate")
    public String activateUser(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User user = userRepository
                .findById(id)
                .orElse(null);

        if (user != null &&
                "USER".equals(user.getRole())) {

            user.setStatus("ACTIVE");

            userRepository.save(user);
        }

        return "redirect:/admin/users";
    }


    // =====================================================
    // DEACTIVATE USER
    // =====================================================

    @PostMapping("/user/{id}/deactivate")
    public String deactivateUser(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User user = userRepository
                .findById(id)
                .orElse(null);

        if (user != null &&
                "USER".equals(user.getRole())) {

            user.setStatus("INACTIVE");

            userRepository.save(user);
        }

        return "redirect:/admin/users";
    }


    // =====================================================
    // DELETE USER
    // =====================================================

    @PostMapping("/user/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User user = userRepository
                .findById(id)
                .orElse(null);

        if (user == null ||
                !"USER".equals(user.getRole())) {

            return "redirect:/admin/users";
        }

        /*
         * Do not delete a user blindly.
         *
         * A booking may still reference this user.
         * Deleting the user in that situation can cause
         * a foreign-key constraint error.
         *
         * For this simple project, deactivate the account
         * instead when the user already has bookings.
         */

        boolean hasBookings =
                !bookingRepository
                        .findByUserId(user.getId())
                        .isEmpty();

        if (hasBookings) {

            user.setStatus("INACTIVE");

            userRepository.save(user);

            return "redirect:/admin/users";
        }

        userRepository.delete(user);

        return "redirect:/admin/users";
    }


    // =====================================================
    // MANAGE OWNERS
    // =====================================================

    @GetMapping("/owners")
    public String manageOwners(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> owners = userRepository.findAll()
                .stream()
                .filter(user ->
                        "OWNER".equals(user.getRole()))
                .toList();

        model.addAttribute(
                "owners",
                owners
        );

        return "admin/owners";
    }


    // =====================================================
    // ACTIVATE OWNER
    // =====================================================

    @PostMapping("/owner/{id}/activate")
    public String activateOwner(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User owner = userRepository
                .findById(id)
                .orElse(null);

        if (owner != null &&
                "OWNER".equals(owner.getRole())) {

            owner.setStatus("ACTIVE");

            userRepository.save(owner);
        }

        return "redirect:/admin/owners";
    }


    // =====================================================
    // DEACTIVATE OWNER
    // =====================================================

    @PostMapping("/owner/{id}/deactivate")
    public String deactivateOwner(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User owner = userRepository
                .findById(id)
                .orElse(null);

        if (owner != null &&
                "OWNER".equals(owner.getRole())) {

            owner.setStatus("INACTIVE");

            userRepository.save(owner);
        }

        return "redirect:/admin/owners";
    }


    // =====================================================
    // VIEW OWNER PROPERTIES
    // =====================================================

    @GetMapping("/owner/{id}/properties")
    public String ownerProperties(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        User owner = userRepository
                .findById(id)
                .orElse(null);

        if (owner == null ||
                !"OWNER".equals(owner.getRole())) {

            return "redirect:/admin/owners";
        }

        List<Property> properties =
                propertyRepository
                        .findByOwnerId(id);

        model.addAttribute(
                "owner",
                owner
        );

        model.addAttribute(
                "properties",
                properties
        );

        return "admin/owner-properties";
    }


    // =====================================================
    // MANAGE PROPERTIES
    // =====================================================

    @GetMapping("/properties")
    public String manageProperties(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Property> properties =
                propertyRepository.findAll();

        model.addAttribute(
                "properties",
                properties
        );

        return "admin/properties";
    }


    // =====================================================
    // ACTIVATE PROPERTY
    // =====================================================

    @PostMapping("/property/{id}/activate")
    public String activateProperty(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Property property =
                propertyRepository
                        .findById(id)
                        .orElse(null);

        if (property != null) {

            property.setStatus("ACTIVE");

            propertyRepository.save(property);
        }

        return "redirect:/admin/properties";
    }


    // =====================================================
    // DEACTIVATE PROPERTY
    // =====================================================

    @PostMapping("/property/{id}/deactivate")
    public String deactivateProperty(
            @PathVariable Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Property property =
                propertyRepository
                        .findById(id)
                        .orElse(null);

        if (property != null) {

            property.setStatus("INACTIVE");

            propertyRepository.save(property);
        }

        return "redirect:/admin/properties";
    }


    // =====================================================
    // MANAGE ROOMS
    // =====================================================

    @GetMapping("/rooms")
    public String manageRooms(
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Room> rooms =
                roomRepository.findAll();

        model.addAttribute(
                "rooms",
                rooms
        );

        return "admin/rooms";
    }


    // =====================================================
    // MANAGE BOOKINGS
    // =====================================================

    @GetMapping("/bookings")
    public String manageBookings(
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Booking> bookings =
                bookingRepository.findAll();

        if (status != null &&
                !status.isBlank()) {

            bookings = bookings
                    .stream()
                    .filter(booking ->
                            status.equals(
                                    booking.getStatus()))
                    .toList();
        }

        model.addAttribute(
                "bookings",
                bookings
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        return "admin/bookings";
    }


    // =====================================================
    // ADMIN CHECK
    // =====================================================

    private boolean isAdmin(
            HttpSession session) {

        String role =
                (String) session.getAttribute(
                        "userRole"
                );

        return "ADMIN".equals(role);
    }
}