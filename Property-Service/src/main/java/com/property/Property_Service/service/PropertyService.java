package com.property.Property_Service.service;

import com.property.Property_Service.dto.*;
import com.property.Property_Service.entity.Property;
import com.property.Property_Service.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository repository;
    private final KafkaTemplate<String, PropertyEvent> kafkaTemplate;

    private static final String PROPERTY_TOPIC = "property-events";


    public PropertyResponse create(PropertyRequest request) {
        Property property = Property.builder()
                .type(request.getType())
                .address(request.getAddress())
                .rentPrice(request.getRentPrice())
                .description(request.getDescription())
                .available(true)
                .build();
        return toResponse(repository.save(property));
    }


    public List<PropertyResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }


    public PropertyResponse getById(Long id) {
        return toResponse(repository.findById(id).orElseThrow());
    }


    public PropertyResponse update(Long id, PropertyRequest request) {
        Property p = repository.findById(id).orElseThrow();
        p.setType(request.getType());
        p.setAddress(request.getAddress());
        p.setRentPrice(request.getRentPrice());
        p.setDescription(request.getDescription());
        return toResponse(repository.save(p));
    }


    public void delete(Long id) {
        repository.deleteById(id);
    }

    public PropertyResponse updateAvailability(Long id, AvailabilityRequest request) {

        Property p = repository.findById(id).orElseThrow();
        p.setAvailable(request.getAvailable());
        Property saved = repository.save(p);

        try {
            PropertyEvent event = new PropertyEvent(
                    saved.getId(),
                    saved.getAvailable() ? "AVAILABLE" : "UNAVAILABLE",
                    saved.getRentPrice(),
                    saved.getAddress(),
                    saved.getDescription().getRooms(),
                    saved.getDescription().getSurface(),
                    saved.getDescription().getCriteria()
            );
            kafkaTemplate.send(PROPERTY_TOPIC, saved.getId().toString(), event);
            log.info("[PropertyService] Événement publié → type={}, propertyId={}",
                    event.getEventType(), saved.getId());
        } catch (Exception e) {
            log.error("[PropertyService] Erreur Kafka (non bloquant) → {}", e.getMessage());
        }

        return toResponse(saved);
    }

    // SEARCH AVANCÉE
    public List<PropertyResponse> search(String keyword,
                                         Integer rooms,
                                         Double minSurface,
                                         Double maxSurface,
                                         Double minPrice,
                                         Double maxPrice,
                                         Boolean available) {

        if (keyword != null && keyword.matches("S\\+\\d+")) {
            rooms = Integer.parseInt(keyword.split("\\+")[1]);
        }

        return repository.advancedSearch(
                keyword,
                rooms,
                minSurface,
                maxSurface,
                minPrice,
                maxPrice,
                available
        ).stream().map(this::toResponse).toList();
    }

    // MAPPER
    private PropertyResponse toResponse(Property p) {
        return PropertyResponse.builder()
                .id(p.getId())
                .type(p.getType())
                .address(p.getAddress())
                .rentPrice(p.getRentPrice())
                .available(p.getAvailable())
                .description(p.getDescription())
                .build();
    }

    public long countAvailableProperties() {
        return repository.countByAvailableTrue();
    }

    public long countUnavailableProperties() {
        return repository.countByAvailableFalse();
    }
}