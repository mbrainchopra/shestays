package org.example.shestay.controller;

import org.example.shestay.entity.Property;
import org.example.shestay.repository.PropertyRepository;
import org.example.shestay.repository.RoomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;

    public PropertyController(
            PropertyRepository propertyRepository,
            RoomRepository roomRepository) {

        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
    }

    // =========================================================
    // PROPERTY SEARCH / LIST
    // =========================================================

    @GetMapping
    public String properties(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String propertyType,
            @RequestParam(required = false) Double maxRent,
            Model model) {

        location = normalize(location);
        propertyType = normalize(propertyType);

        if (maxRent != null && maxRent < 0) {
            maxRent = null;
        }

        List<Property> properties =
                propertyRepository.searchActiveProperties(
                        location,
                        propertyType,
                        maxRent
                );

        model.addAttribute(
                "properties",
                properties
        );

        model.addAttribute(
                "location",
                location
        );

        model.addAttribute(
                "propertyType",
                propertyType
        );

        model.addAttribute(
                "maxRent",
                maxRent
        );

        return "properties";
    }

    // =========================================================
    // PROPERTY DETAILS
    // =========================================================

    @GetMapping("/{id}")
    public String propertyDetails(
            @PathVariable Long id,
            Model model) {

        Property property =
                propertyRepository
                        .findById(id)
                        .orElse(null);

        if (property == null
                || !"ACTIVE".equals(property.getStatus())) {

            return "redirect:/properties";
        }

        model.addAttribute(
                "property",
                property
        );

        model.addAttribute(
                "rooms",
                roomRepository.findByPropertyId(id)
        );

        return "property-details";
    }

    // =========================================================
    // NORMALIZE SEARCH VALUE
    // =========================================================

    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isBlank()
                ? null
                : trimmed;
    }
}