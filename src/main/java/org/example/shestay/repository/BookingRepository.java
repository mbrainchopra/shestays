package org.example.shestay.repository;

import org.example.shestay.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByPropertyOwnerId(Long ownerId);

    List<Booking> findByPropertyId(Long propertyId);

    boolean existsByUserIdAndRoomIdAndStatus(Long userId, Long roomId, String status);

    long countByStatus(String status);
}
