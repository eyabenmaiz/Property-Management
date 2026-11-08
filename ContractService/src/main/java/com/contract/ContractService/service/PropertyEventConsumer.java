package com.contract.ContractService.service;

import com.contract.ContractService.dto.PropertyEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "property-events", groupId = "contract-group")
    public void handlePropertyEvent(byte[] payload) {
        try {
            PropertyEvent event = objectMapper.readValue(payload, PropertyEvent.class);
            log.info("[ContractService] Événement reçu → type={}, propertyId={}",
                    event.getEventType(), event.getPropertyId());
        } catch (Exception e) {
            log.error("[ContractService] Erreur désérialisation PropertyEvent", e);
        }
    }
}