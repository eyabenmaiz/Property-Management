package com.payment.payment_service.repository;



import com.payment.payment_service.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ─── Finder Methods ───────────────────────────────────────────────────────

    // Tous les paiements d'un contrat
    List<Payment> findByContractId(String contractId);

    // Tous les paiements d'une propriété
    List<Payment> findByPropertyId(Long propertyId);
    // Paiements PENDING en retard
    List<Payment> findByStatusAndPaymentDateBefore(PaymentStatus status, LocalDate date);

    // Paiements PENDING à venir dans X jours
    List<Payment> findByStatusAndPaymentDateBetween(PaymentStatus status, LocalDate start, LocalDate end);
    // Paiements par statut
    List<Payment> findByStatus(PaymentStatus status);

    // Paiements d'un contrat par statut
    List<Payment> findByContractIdAndStatus(String contractId, PaymentStatus status);

    // Paiements entre deux dates
    List<Payment> findByPaymentDateBetween(LocalDate start, LocalDate end);

    // Paiements d'une propriété entre deux dates
    List<Payment> findByPropertyIdAndPaymentDateBetween(
            Long propertyId, LocalDate start, LocalDate end);

    // Vérifier doublon (même contrat + même date + même statut)
    boolean existsByContractIdAndPaymentDateAndStatus(
            String contractId, LocalDate paymentDate, PaymentStatus status);

    // ─── JPQL Queries ─────────────────────────────────────────────────────────

    // Total payé (COMPLETED) pour un contrat
    @Query("SELECT SUM(p.amount) FROM Payment p " +
            "WHERE p.contractId = :contractId AND p.status = 'COMPLETED'")
    Double getTotalPaidByContract(@Param("contractId") String contractId);

    // Total payé (COMPLETED) pour une propriété
    @Query("SELECT SUM(p.amount) FROM Payment p " +
            "WHERE p.propertyId = :propertyId AND p.status = 'COMPLETED'")
    Double getTotalPaidByProperty(@Param("propertyId") Long propertyId);

    // Nombre de paiements COMPLETED pour un contrat
    @Query("SELECT COUNT(p) FROM Payment p " +
            "WHERE p.contractId = :contractId AND p.status = 'COMPLETED'")
    Long countCompletedByContract(@Param("contractId") String contractId);
    // vérifier si paiement COMPLETED existe
    boolean existsByContractIdAndStatus(String contractId, PaymentStatus status);
    List<Payment> findByContractIdInAndStatus(List<String> contractIds, PaymentStatus status);

    // Recherche combinée (tous les paramètres optionnels)
    @Query("SELECT p FROM Payment p WHERE " +
            "(:contractId IS NULL OR p.contractId = :contractId) AND " +
            "(:propertyId IS NULL OR p.propertyId = :propertyId) AND " +
            "(:status IS NULL OR p.status = :status)")
    List<Payment> searchPayments(
            @Param("contractId") String contractId,
            @Param("propertyId") Long propertyId,
            @Param("status") PaymentStatus status);
}