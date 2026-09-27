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

@Controller
@RequestMapping("/owner")
public class OwnerController {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public OwnerController(
            PropertyRepository propertyRepository,
            UserRepository userRepository,
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {

        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    // =========================================================
    // OWNER REGISTRATION
    // =========================================================

    @GetMapping("/register")
    public String showOwnerRegisterPage(Model model) {

        model.addAttribute("user", new User());

        return "owner/register";
    }

    @PostMapping("/register")
    public String registerOwner(
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

        if (name.isBlank()
                || email.isBlank()
                || password.isBlank()) {

            model.addAttribute(
                    "error",
                    "Please complete all required fields."
            );

            return "owner/register";
        }

        if (password.length() < 6) {

            model.addAttribute(
                    "error",
                    "Password must be at least 6 characters."
            );

            return "owner/register";
        }

        if (userRepository.existsByEmail(email)) {

            model.addAttribute(
                    "error",
                    "Email already registered."
            );

            return "owner/register";
        }

        user.setName(name);
        user.setEmail(email);
        user.setRole("OWNER");
        user.setStatus("ACTIVE");

        userRepository.save(user);

        return "redirect:/login";
    }

    // =========================================================
    // OWNER DASHBOARD
    // =========================================================

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Long ownerId =
                (Long) session.getAttribute("userId");

        model.addAttribute(
                "propertyCount",
                propertyRepository.findByOwnerId(ownerId).size()
        );

        model.addAttribute(
                "bookingCount",
                bookingRepository
                        .findByPropertyOwnerId(ownerId)
                        .size()
        );

        model.addAttribute(
                "ownerName",
                session.getAttribute("userName")
        );

        return "owner/dashboard";
    }

    // =========================================================
    // ADD PROPERTY - FORM
    // =========================================================

    @GetMapping("/add-property")
    public String showAddPropertyPage(
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "property",
                new Property()
        );

        return "owner/add-property";
    }

    // =========================================================
    // ADD PROPERTY
    // =========================================================

    @PostMapping("/add-property")
    public String addProperty(
            @ModelAttribute("property") Property property,
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        String name = property.getName() == null
                ? ""
                : property.getName().trim();

        String location = property.getLocation() == null
                ? ""
                : property.getLocation().trim();

        String propertyType = property.getPropertyType() == null
                ? ""
                : property.getPropertyType().trim();

        if (name.isBlank()
                || location.isBlank()
                || propertyType.isBlank()
                || property.getRent() == null
                || property.getRent() < 0) {

            model.addAttribute(
                    "error",
                    "Please complete all required property details."
            );

            return "owner/add-property";
        }

        Long ownerId =
                (Long) session.getAttribute("userId");

        User owner = userRepository
                .findById(ownerId)
                .orElse(null);

        if (owner == null
                || !"OWNER".equals(owner.getRole())
                || !"ACTIVE".equals(owner.getStatus())) {

            return "redirect:/login";
        }

        property.setName(name);
        property.setLocation(location);
        property.setPropertyType(propertyType);
        property.setOwner(owner);
        property.setStatus("ACTIVE");

        propertyRepository.save(property);

        return "redirect:/owner/properties";
    }

    // =========================================================
    // OWNER PROPERTIES
    // =========================================================

    @GetMapping("/properties")
    public String myProperties(
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Long ownerId =
                (Long) session.getAttribute("userId");

        model.addAttribute(
                "properties",
                propertyRepository.findByOwnerId(ownerId)
        );

        return "owner/properties";
    }

    // =========================================================
    // ADD ROOM - FORM
    // =========================================================

    @GetMapping("/property/{propertyId}/add-room")
    public String showAddRoomPage(
            @PathVariable Long propertyId,
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Property property =
                getOwnedProperty(propertyId, session);

        if (property == null
                || !"ACTIVE".equals(property.getStatus())) {

            return "redirect:/owner/properties";
        }

        Room room = new Room();

        room.setProperty(property);

        model.addAttribute("room", room);
        model.addAttribute("property", property);

        return "owner/add-room";
    }

    // =========================================================
    // ADD ROOM
    // =========================================================

    @PostMapping("/property/{propertyId}/add-room")
    public String addRoom(
            @PathVariable Long propertyId,
            @ModelAttribute("room") Room room,
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Property property =
                getOwnedProperty(propertyId, session);

        if (property == null
                || !"ACTIVE".equals(property.getStatus())) {

            return "redirect:/owner/properties";
        }

        String roomType = room.getRoomType() == null
                ? ""
                : room.getRoomType().trim();

        if (roomType.isBlank()
                || room.getCapacity() == null
                || room.getCapacity() <= 0
                || room.getTotalRooms() == null
                || room.getTotalRooms() <= 0
                || room.getPrice() == null
                || room.getPrice() < 0) {

            model.addAttribute(
                    "error",
                    "Please enter valid room details."
            );

            model.addAttribute(
                    "property",
                    property
            );

            return "owner/add-room";
        }

        room.setRoomType(roomType);
        room.setProperty(property);

        room.setAvailableRooms(
                room.getTotalRooms()
        );

        room.setStatus("AVAILABLE");

        roomRepository.save(room);

        return "redirect:/owner/property/"
                + propertyId
                + "/rooms";
    }

    // =========================================================
    // VIEW PROPERTY ROOMS
    // =========================================================

    @GetMapping("/property/{propertyId}/rooms")
    public String viewRooms(
            @PathVariable Long propertyId,
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Property property =
                getOwnedProperty(propertyId, session);

        if (property == null) {
            return "redirect:/owner/properties";
        }

        model.addAttribute(
                "property",
                property
        );

        model.addAttribute(
                "rooms",
                roomRepository.findByPropertyId(propertyId)
        );

        return "owner/rooms";
    }

    // =========================================================
    // BOOKING REQUESTS
    // =========================================================

    @GetMapping("/bookings")
    public String bookingRequests(
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Long ownerId =
                (Long) session.getAttribute("userId");

        model.addAttribute(
                "bookings",
                bookingRepository
                        .findByPropertyOwnerId(ownerId)
        );

        return "owner/bookings";
    }

    // =========================================================
    // APPROVE BOOKING
    // =========================================================

    @PostMapping("/booking/{bookingId}/approve")
    public String approveBooking(
            @PathVariable Long bookingId,
            HttpSession session,
            Model model) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Long ownerId =
                (Long) session.getAttribute("userId");

        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElse(null);

        if (!belongsToOwner(booking, ownerId)
                || !"REQUESTED".equals(booking.getStatus())) {

            return "redirect:/owner/bookings";
        }

        // -----------------------------------------------------
        // CHECK PROPERTY
        // -----------------------------------------------------

        Property property =
                booking.getProperty();

        if (property == null
                || !"ACTIVE".equals(property.getStatus())) {

            return showBookingError(
                    "This property is currently inactive.",
                    ownerId,
                    model
            );
        }

        // -----------------------------------------------------
        // CHECK CHECK-IN DATE
        // -----------------------------------------------------

        if (booking.getCheckInDate() != null
                && booking.getCheckInDate()
                .isBefore(LocalDate.now())) {

            return showBookingError(
                    "This booking has a past check-in date and cannot be approved.",
                    ownerId,
                    model
            );
        }

        // -----------------------------------------------------
        // CHECK ROOM
        // -----------------------------------------------------

        Room room =
                booking.getRoom();

        if (room == null
                || room.getAvailableRooms() == null
                || room.getAvailableRooms() <= 0) {

            return showBookingError(
                    "No rooms are currently available for this booking.",
                    ownerId,
                    model
            );
        }

        // -----------------------------------------------------
        // APPROVE
        // -----------------------------------------------------

        booking.setStatus("APPROVED");

        room.setAvailableRooms(
                room.getAvailableRooms() - 1
        );

        if (room.getAvailableRooms() == 0) {
            room.setStatus("FULL");
        } else {
            room.setStatus("AVAILABLE");
        }

        roomRepository.save(room);
        bookingRepository.save(booking);

        return "redirect:/owner/bookings";
    }

    // =========================================================
    // REJECT BOOKING
    // =========================================================

    @PostMapping("/booking/{bookingId}/reject")
    public String rejectBooking(
            @PathVariable Long bookingId,
            HttpSession session) {

        if (!isOwner(session)) {
            return "redirect:/login";
        }

        Long ownerId =
                (Long) session.getAttribute("userId");

        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElse(null);

        if (!belongsToOwner(booking, ownerId)
                || !"REQUESTED".equals(booking.getStatus())) {

            return "redirect:/owner/bookings";
        }

        booking.setStatus("REJECTED");

        bookingRepository.save(booking);

        return "redirect:/owner/bookings";
    }

    // =========================================================
    // GET OWNER'S PROPERTY
    // =========================================================

    private Property getOwnedProperty(
            Long propertyId,
            HttpSession session) {

        Long ownerId =
                (Long) session.getAttribute("userId");

        Property property =
                propertyRepository
                        .findById(propertyId)
                        .orElse(null);

        if (property == null
                || property.getOwner() == null
                || !property.getOwner()
                .getId()
                .equals(ownerId)) {

            return null;
        }

        return property;
    }

    // =========================================================
    // CHECK BOOKING OWNERSHIP
    // =========================================================

    private boolean belongsToOwner(
            Booking booking,
            Long ownerId) {

        return booking != null
                && booking.getProperty() != null
                && booking.getProperty().getOwner() != null
                && booking.getProperty()
                .getOwner()
                .getId()
                .equals(ownerId);
    }

    // =========================================================
    // BOOKING ERROR PAGE
    // =========================================================

    private String showBookingError(
            String error,
            Long ownerId,
            Model model) {

        model.addAttribute(
                "error",
                error
        );

        model.addAttribute(
                "bookings",
                bookingRepository
                        .findByPropertyOwnerId(ownerId)
        );

        return "owner/bookings";
    }

    // =========================================================
    // OWNER SESSION CHECK
    // =========================================================

    private boolean isOwner(
            HttpSession session) {

        return "OWNER".equals(
                session.getAttribute("userRole")
        );
    }
}