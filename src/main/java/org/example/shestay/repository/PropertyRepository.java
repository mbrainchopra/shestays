package org.example.shestay.repository;

import org.example.shestay.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByLocationContainingIgnoreCase(String location);

    List<Property> findByLocationContainingIgnoreCaseAndStatus(
            String location,
            String status
    );

    List<Property> findByPropertyType(String propertyType);

    List<Property> findByPropertyTypeAndStatus(
            String propertyType,
            String status
    );

    List<Property> findByOwnerId(Long ownerId);

    List<Property> findByStatus(String status);

    @Query("""
            SELECT p
            FROM Property p
            WHERE p.status = 'ACTIVE'
            AND (
                :location IS NULL
                OR :location = ''
                OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))
            )
            AND (
                :propertyType IS NULL
                OR :propertyType = ''
                OR p.propertyType = :propertyType
            )
            AND (
                :maxRent IS NULL
                OR p.rent <= :maxRent
            )
            ORDER BY p.id DESC
            """)
    List<Property> searchActiveProperties(
            @Param("location") String location,
            @Param("propertyType") String propertyType,
            @Param("maxRent") Double maxRent
    );
}