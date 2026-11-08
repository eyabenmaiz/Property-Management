package com.property.Property_Service.repository;

import com.property.Property_Service.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    long countByAvailableTrue();

    long countByAvailableFalse();


    List<Property> findByAvailableTrue();

    List<Property> findByAvailableFalse();


    List<Property> findByTypeIgnoreCase(String type);


    List<Property> findByAddressContainingIgnoreCase(String keyword);

    List<Property> findByTypeIgnoreCaseAndAvailableTrue(String type);

    List<Property> findByRentPriceLessThanEqual(Double maxPrice);
    List<Property> findByAvailableTrueAndRentPriceLessThanEqual(Double maxPrice);

    boolean existsByAddressIgnoreCase(String address);

    @Query("""
        SELECT p FROM Property p WHERE
        (:keyword IS NULL OR 
         LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(p.description.criteria) LIKE LOWER(CONCAT('%', :keyword, '%')))

        AND (:rooms IS NULL OR p.description.rooms = :rooms)

        AND (:minSurface IS NULL OR p.description.surface >= :minSurface)
        AND (:maxSurface IS NULL OR p.description.surface <= :maxSurface)

        AND (:minPrice IS NULL OR p.rentPrice >= :minPrice)
        AND (:maxPrice IS NULL OR p.rentPrice <= :maxPrice)

        AND (:available IS NULL OR p.available = :available)
    """)
    List<Property> advancedSearch(
            @Param("keyword") String keyword,
            @Param("rooms") Integer rooms,
            @Param("minSurface") Double minSurface,
            @Param("maxSurface") Double maxSurface,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("available") Boolean available
    );
}