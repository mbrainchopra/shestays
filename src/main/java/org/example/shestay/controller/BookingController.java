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

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;

    public BookingController(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            PropertyRepository propertyRepository,
            RoomRepository roomRepository) {

        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
    }

    @GetMapping("/property/{propertyId}/room/{roomId}")
    public String showBookingPage(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            HttpSession session,
            Model model) {

        if (!isUser(session)) {
            return "redirect:/login";
        }

        Property property = propertyRepository
                .findById(propertyId)
                .orElse(null);

        Room room = roomRepository
                .findById(roomId)
                .orElse(null);

        if (!isValidRoom(property, room)) {
            return "redirect:/properties";
        }

        if (room.getAvailableRooms() == null ||
                room.getAvailableRooms() <= 0) {

            return "redirect:/properties/" + propertyId;
        }

        Long userId = (Long) session.getAttribute("userId");

        if (bookingRepository.existsByUserIdAndRoomIdAndStatus(
                userId, roomId, "REQUESTED") ||
                bookingRepository.existsByUserIdAndRoomIdAndStatus(
                        userId, roomId, "APPROVED")) {

            return "redirect:/bookings/my-bookings";
        }

        model.addAttribute("property", property);
        model.addAttribute("room", room);

        return "booking-form";
    }

    @PostMapping("/property/{propertyId}/room/{roomId}")
    public String createBooking(
            @PathVariable Long propertyId,
            @PathVariable Long roomId,
            @RequestParam String checkInDate,
            @RequestParam(required = false) String remarks,
            HttpSession session,
            Model model) {

        if (!isUser(session)) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("userId");

        User user = userRepository
                .findById(userId)
                .orElse(null);

        Property property = propertyRepository
                .findById(propertyId)
                .orElse(null);

        Room room = roomRepository
                .findById(roomId)
                .orElse(null);

        if (user == null || !isValidRoom(property, room)) {
            return "redirect:/properties";
        }

        if (room.getAvailableRooms() == null ||
                room.getAvailableRooms() <= 0) {

            return "redirect:/properties/" + propertyId;
        }

        if (bookingRepository.existsByUserIdAndRoomIdAndStatus(
                userId, roomId, "REQUESTED") ||
                bookingRepository.existsByUserIdAndRoomIdAndStatus(
                        userId, roomId, "APPROVED")) {

            model.addAttribute(
                    "error",
                    "You already have an active booking request for this room."
            );

            model.addAttribute("property", property);
            model.addAttribute("room", room);

            return "booking-form";
        }

        LocalDate checkIn;

        try {
            checkIn = LocalDate.parse(checkInDate);

        } catch (DateTimeParseException ex) {

            model.addAttribute(
                    "error",
                    "Please select a valid check-in date."
            );

            model.addAttribute("property", property);
            model.addAttribute("room", room);

            return "booking-form";
        }

        if (checkIn.isBefore(LocalDate.now())) {

            model.addAttribute(
                    "error",
                    "Check-in date cannot be in the past."
            );

            model.addAttribute("property", property);
            model.addAttribute("room", room);

            return "booking-form";
        }

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setProperty(property);
        booking.setRoom(room);
        booking.setBookingDate(LocalDate.now());
        booking.setCheckInDate(checkIn);
        booking.setStatus("REQUESTED");
        booking.setRemarks(
                remarks == null ? null : remarks.trim()
        );

        bookingRepository.save(booking);

        return "redirect:/bookings/my-bookings";
    }

    @GetMapping("/my-bookings")
    public String myBookings(
            HttpSession session,
            Model model) {

        if (!isUser(session)) {
            return "redirect:/login";
        }

        Long userId =
                (Long) session.getAttribute("userId");

        model.addAttribute(
                "bookings",
                bookingRepository.findByUserId(userId)
        );

        return "my-bookings";
    }

    private boolean isValidRoom(
            Property property,
            Room room) {

        return property != null &&
                "ACTIVE".equals(property.getStatus()) &&
                room != null &&
                room.getProperty() != null &&
                room.getProperty()
                        .getId()
                        .equals(property.getId());
    }

    private boolean isUser(HttpSession session) {

        return "USER".equals(
                session.getAttribute("userRole")
        );
    }
}