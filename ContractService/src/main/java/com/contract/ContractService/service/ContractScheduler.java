package com.contract.ContractService.service;

import com.contract.ContractService.dto.ContractEvent;
import com.contract.ContractService.entity.Contract;
import com.contract.ContractService.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractScheduler {

    private final ContractRepository contractRepository;
    private final KafkaTemplate<String, ContractEvent> kafkaTemplate;

    private static final String CONTRACT_TOPIC = "contract-events";

    // CONTRATS EXPIRÉS
    @Scheduled(cron = "${scheduler.contract.expired.cron:0 0 0 * * *}")
    @Transactional
    public void checkExpiredContracts() {

        LocalDate today = LocalDate.now();
        log.info("[Scheduler] Vérification des contrats expirés → date={}", today);

        List<Contract> expiredContracts = contractRepository
                .findByActiveTrueAndEndDateBefore(today);

        if (expiredContracts.isEmpty()) {
            log.info("[Scheduler] Aucun contrat expiré trouvé");
            return;
        }

        log.info("[Scheduler] {} contrat(s) expiré(s) trouvé(s)", expiredContracts.size());

        for (Contract contract : expiredContracts) {
            try {
                contract.setActive(false);
                contractRepository.save(contract);

                log.warn("[Scheduler] Contrat {} expiré → marqué inactif, propertyId={}",
                        contract.getId(), contract.getPropertyId());

                kafkaTemplate.send(CONTRACT_TOPIC, contract.getId(), new ContractEvent(
                        contract.getId(),
                        "CONTRACT_EXPIRED",
                        contract.getTenantId(),
                        contract.getPropertyId(),
                        contract.getMonthlyRent()
                ));

                log.info("[Scheduler] Événement CONTRACT_EXPIRED publié → contractId={}",
                        contract.getId());

            } catch (Exception e) {
                log.error("[Scheduler] Erreur traitement contrat {} → {}",
                        contract.getId(), e.getMessage());
            }
        }
    }

    // ─── CONTRATS EXPIRANT BIENTÔT
    @Scheduled(cron = "${scheduler.contract.expiring.cron:0 0 9 * * *}")
    @Transactional(readOnly = true)
    public void checkExpiringContracts() {

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(30);

        log.info("[Scheduler] Vérification des contrats expirant dans 30 jours");

        List<Contract> expiringContracts = contractRepository
                .findByActiveTrueAndEndDateBetween(today, in30Days);

        if (expiringContracts.isEmpty()) {
            log.info("[Scheduler] Aucun contrat expirant bientôt");
            return;
        }

        log.info("[Scheduler] {} contrat(s) expirant bientôt", expiringContracts.size());

        for (Contract contract : expiringContracts) {
            try {
                long daysLeft = ChronoUnit.DAYS.between(today, contract.getEndDate());

                log.warn("[Scheduler] Contrat {} expire dans {} jours → propertyId={}",
                        contract.getId(), daysLeft, contract.getPropertyId());

                kafkaTemplate.send(CONTRACT_TOPIC, contract.getId(), new ContractEvent(
                        contract.getId(),
                        "CONTRACT_EXPIRING_SOON",
                        contract.getTenantId(),
                        contract.getPropertyId(),
                        contract.getMonthlyRent()
                ));

                log.info("[Scheduler] Événement CONTRACT_EXPIRING_SOON publié → contractId={}, daysLeft={}",
                        contract.getId(), daysLeft);

            } catch (Exception e) {
                log.error("[Scheduler] Erreur alerte contrat {} → {}",
                        contract.getId(), e.getMessage());
            }
        }
    }
}