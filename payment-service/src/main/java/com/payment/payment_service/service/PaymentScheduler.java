package com.payment.payment_service.service;

import com.payment.payment_service.dto.PaymentEvent;
import com.payment.payment_service.entity.Payment;
import com.payment.payment_service.entity.PaymentStatus;
import com.payment.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-events";

    // ─── PAIEMENTS EN RETARD ──────────────────────────────────────────────────
    @Scheduled(cron = "${scheduler.payment.overdue.cron:0 0 8 * * *}")
    @Transactional
    public void checkOverduePayments() {

        LocalDate today = LocalDate.now();
        log.info("[Scheduler] Vérification des paiements en retard → date={}", today);

        List<Payment> overduePayments = paymentRepository
                .findByStatusAndPaymentDateBefore(PaymentStatus.PENDING, today);

        if (overduePayments.isEmpty()) {
            log.info("[Scheduler] Aucun paiement en retard trouvé");
            return;
        }

        log.warn("[Scheduler] {} paiement(s) en retard trouvé(s)", overduePayments.size());

        for (Payment payment : overduePayments) {
            try {
                log.warn("[Scheduler] Paiement {} en retard → contractId={}, date={}",
                        payment.getId(), payment.getContractId(), payment.getPaymentDate());

                PaymentEvent event = new PaymentEvent(
                        payment.getId(),
                        "PAYMENT_OVERDUE",
                        payment.getContractId(),
                        payment.getPropertyId(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getMethod(),
                        payment.getPaymentDate()
                );
                kafkaTemplate.send(PAYMENT_TOPIC, payment.getId().toString(), event);

                log.info("[Scheduler] Événement PAYMENT_OVERDUE publié → paymentId={}",
                        payment.getId());

            } catch (Exception e) {
                log.error("[Scheduler] Erreur paiement {} → {}",
                        payment.getId(), e.getMessage());
            }
        }
    }

    // ─── RAPPEL PAIEMENT DANS 3 JOURS ─────────────────────────────────────────
    @Scheduled(cron = "${scheduler.payment.upcoming.cron:0 0 10 * * *}")
    @Transactional(readOnly = true)
    public void checkUpcomingPayments() {

        LocalDate today = LocalDate.now();
        LocalDate in3Days = today.plusDays(3);

        log.info("[Scheduler] Vérification des paiements à venir dans 3 jours");

        List<Payment> upcomingPayments = paymentRepository
                .findByStatusAndPaymentDateBetween(PaymentStatus.PENDING, today, in3Days);

        if (upcomingPayments.isEmpty()) {
            log.info("[Scheduler] Aucun paiement à venir dans 3 jours");
            return;
        }

        log.info("[Scheduler] {} paiement(s) à venir dans 3 jours", upcomingPayments.size());

        for (Payment payment : upcomingPayments) {
            try {
                log.info("[Scheduler] Rappel paiement {} → contractId={}, date={}",
                        payment.getId(), payment.getContractId(), payment.getPaymentDate());

                PaymentEvent event = new PaymentEvent(
                        payment.getId(),
                        "PAYMENT_DUE_SOON",
                        payment.getContractId(),
                        payment.getPropertyId(),
                        payment.getAmount(),
                        payment.getStatus(),
                        payment.getMethod(),
                        payment.getPaymentDate()
                );
                kafkaTemplate.send(PAYMENT_TOPIC, payment.getId().toString(), event);

                log.info("[Scheduler] Événement PAYMENT_DUE_SOON publié → paymentId={}",
                        payment.getId());

            } catch (Exception e) {
                log.error("[Scheduler] Erreur rappel paiement {} → {}",
                        payment.getId(), e.getMessage());
            }
        }
    }
}