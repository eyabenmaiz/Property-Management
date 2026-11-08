package com.payment.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.payment_service.dto.ContractEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "contract-events", groupId = "payment-group")
    public void handleContractEvent(byte[] payload) {
        try {
            ContractEvent event = objectMapper.readValue(payload, ContractEvent.class);

            log.info("[PaymentService] Événement Contract reçu → type={}, contractId={}, loyer={}",
                    event.getEventType(), event.getContractId(), event.getMonthlyRent());

            switch (event.getEventType()) {
                case "CONTRACT_CREATED" ->
                        log.info("[PaymentService] Nouveau contrat → loyer mensuel attendu : {}",
                                event.getMonthlyRent());

                case "CONTRACT_TERMINATED", "CONTRACT_EXPIRED" ->
                        log.warn("[PaymentService] Contrat terminé → plus de paiements pour : {}",
                                event.getContractId());

                case "CONTRACT_EXPIRING_SOON" ->
                        log.info("[PaymentService] Contrat {} expire bientôt → prévoir dernier paiement",
                                event.getContractId());

                default ->
                        log.warn("[PaymentService] Type d'événement inconnu : {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("[PaymentService] Erreur désérialisation ContractEvent → {}", e.getMessage(), e);
        }
    }
}