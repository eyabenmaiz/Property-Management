package com.property.Property_Service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.property.Property_Service.dto.ContractEvent;
import com.property.Property_Service.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractEventConsumer {

    private final ObjectMapper objectMapper;
    private final PropertyRepository repository;

    @KafkaListener(topics = "contract-events", groupId = "property-group")
    public void handleContractEvent(byte[] payload) {
        try {
            ContractEvent event = objectMapper.readValue(payload, ContractEvent.class);

            log.info("[PropertyService] Événement Contract reçu → type={}, propertyId={}",
                    event.getEventType(), event.getPropertyId());

            if ("CONTRACT_TERMINATED".equals(event.getEventType()) ||
                    "CONTRACT_EXPIRED".equals(event.getEventType())) {

                repository.findById(event.getPropertyId()).ifPresentOrElse(
                        property -> {
                            property.setAvailable(true);
                            repository.save(property);
                            log.info("[PropertyService] Propriété {} remise en AVAILABLE → raison={}",
                                    event.getPropertyId(), event.getEventType());
                        },
                        () -> log.warn("[PropertyService] Propriété {} introuvable",
                                event.getPropertyId())
                );
            }

        } catch (Exception e) {
            log.error("[PropertyService] Erreur désérialisation ContractEvent → {}", e.getMessage(), e);
        }
    }
}