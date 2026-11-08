package com.contract.ContractService.service;

import com.contract.ContractService.client.PropertyClient;
import com.contract.ContractService.dto.*;
import com.contract.ContractService.entity.Contract;
import com.contract.ContractService.exception.*;
import com.contract.ContractService.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final PropertyClient propertyClient;
    private final KafkaTemplate<String, ContractEvent> kafkaTemplate;

    private static final String CONTRACT_TOPIC = "contract-events";

    // ───────── CREATE CONTRACT ─────────
    public ContractResponse createContract(ContractRequest request) {

        Long propertyId = request.getPropertyId();
        PropertyResponse property = getPropertySafely(propertyId);

        if (!property.isAvailable()) {
            throw new PropertyNotAvailableException(propertyId.toString());
        }

        if (contractRepository.existsByPropertyIdAndActiveTrue(propertyId)) {
            throw new ActiveContractException(propertyId);
        }


        try {
            propertyClient.updateAvailability(propertyId, new AvailabilityRequest(false));
            log.info("[ContractService] Propriété {} marquée indisponible", propertyId);
        } catch (Exception e) {
            log.error("[ContractService] Erreur update availability → {}", e.getMessage());
            throw new RuntimeException("Failed to update property availability: " + e.getMessage());
        }


        Contract contract = new Contract();
        contract.setTenantId(request.getTenantId());
        contract.setPropertyId(propertyId);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setMonthlyRent(property.getRentPrice());
        contract.setActive(true);

        Contract saved = contractRepository.save(contract);
        log.info("[ContractService] Contrat créé → id={}", saved.getId());


        try {
            kafkaTemplate.send(CONTRACT_TOPIC, saved.getId(), new ContractEvent(
                    saved.getId(),
                    "CONTRACT_CREATED",
                    saved.getTenantId(),
                    saved.getPropertyId(),
                    saved.getMonthlyRent()
            ));
            log.info("[ContractService] Événement publié → CONTRACT_CREATED, contractId={}", saved.getId());
        } catch (Exception e) {
            log.error("[ContractService] Erreur Kafka (non bloquant) → {}", e.getMessage());
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getAllContracts() {
        return contractRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public ContractResponse getContractById(String id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));
        return mapToResponse(contract);
    }


    public ContractResponse terminateContract(String id) {

        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract not found: " + id));

        if (!contract.isActive()) {
            throw new ActiveContractException(contract.getPropertyId());
        }

        contract.setActive(false);
        Contract updated = contractRepository.save(contract);
        log.info("[ContractService] Contrat terminé → id={}", updated.getId());

        try {
            propertyClient.updateAvailability(contract.getPropertyId(), new AvailabilityRequest(true));
            log.info("[ContractService] Propriété {} libérée", contract.getPropertyId());
        } catch (Exception e) {
            log.error("[ContractService] Erreur release property → {}", e.getMessage());
            throw new RuntimeException("Failed to release property: " + e.getMessage());
        }


        try {
            kafkaTemplate.send(CONTRACT_TOPIC, updated.getId(), new ContractEvent(
                    updated.getId(),
                    "CONTRACT_TERMINATED",
                    updated.getTenantId(),
                    updated.getPropertyId(),
                    updated.getMonthlyRent()
            ));
            log.info("[ContractService] Événement publié → CONTRACT_TERMINATED, contractId={}", updated.getId());
        } catch (Exception e) {
            log.error("[ContractService] Erreur Kafka (non bloquant) → {}", e.getMessage());
        }

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<AvailableSoonResponse> getUpcomingAvailableProperties(int days) {

        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(days);

        List<Contract> contracts =
                contractRepository.findByActiveTrueAndEndDateBetween(today, targetDate);

        return contracts.stream().map(contract -> {
            long daysRemaining = ChronoUnit.DAYS.between(today, contract.getEndDate());
            PropertyResponse property;
            try {
                property = propertyClient.getPropertyById(contract.getPropertyId());
            } catch (Exception e) {
                log.warn("[ContractService] Propriété non récupérée pour contrat {} → {}",
                        contract.getId(), e.getMessage());
                property = null;
            }
            return new AvailableSoonResponse(daysRemaining, contract.getEndDate(), property);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getActiveContracts() {
        return contractRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContractResponse> getInactiveContracts() {
        return contractRepository.findByActiveFalse()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private PropertyResponse getPropertySafely(Long propertyId) {
        try {
            log.info("[ContractService] Appel Property-Service → propertyId={}", propertyId);
            PropertyResponse response = propertyClient.getPropertyById(propertyId);
            log.info("[ContractService] Propriété reçue → id={}, available={}",
                    response.getId(), response.isAvailable());
            return response;
        } catch (Exception e) {
            log.error("[ContractService] Erreur Property-Service → type={}, message={}",
                    e.getClass().getName(), e.getMessage());
            throw new RuntimeException("Property-Service error: " + e.getMessage());
        }
    }

    private ContractResponse mapToResponse(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getTenantId(),
                contract.getPropertyId(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getMonthlyRent(),
                contract.isActive()
        );
    }
}