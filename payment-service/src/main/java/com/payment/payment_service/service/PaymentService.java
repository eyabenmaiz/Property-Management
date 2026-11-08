package com.payment.payment_service.service;

import com.payment.payment_service.dto.*;
import com.payment.payment_service.entity.*;
import com.payment.payment_service.exception.*;
import com.payment.payment_service.feign.*;
import com.payment.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ContractClient contractClient;
    private final PropertyClient propertyClient;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-events";


    public PaymentResponse create(PaymentRequest request) {


        ContractResponse contract;
        try {
            contract = contractClient.getContractById(request.getContractId());
        } catch (Exception e) {
            throw new ContractNotFoundException(request.getContractId());
        }


        if (!contract.isActive()) {
            throw new InvalidPaymentException(
                    "Impossible de créer un paiement : le contrat "
                            + request.getContractId() + " est résilié.");
        }


        try {
            propertyClient.getPropertyById(request.getPropertyId());
        } catch (Exception e) {
            throw new InvalidPaymentException(
                    "Propriété introuvable avec l'ID : " + request.getPropertyId());
        }


        if (paymentRepository.existsByContractIdAndPaymentDateAndStatus(
                request.getContractId(),
                request.getPaymentDate(),
                PaymentStatus.COMPLETED)) {
            throw new PaymentAlreadyExistsException(request.getContractId());
        }

        Payment payment = Payment.builder()
                .contractId(request.getContractId())
                .propertyId(request.getPropertyId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .method(request.getMethod())
                .paymentDate(request.getPaymentDate())
                .description(request.getDescription())
                .build();

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getByContract(String contractId) {
        return paymentRepository.findByContractId(contractId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<PaymentResponse> getByProperty(Long propertyId) {
        return paymentRepository.findByPropertyId(propertyId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── GET BY STATUS ────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─── UPDATE STATUS — publie un événement Kafka ────────────────────────────
    public PaymentResponse updateStatus(Long id, PaymentStatusRequest request) {
        Payment payment = findOrThrow(id);

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new InvalidPaymentException(
                    "Un paiement remboursé ne peut plus être modifié.");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED
                && request.getStatus() == PaymentStatus.PENDING) {
            throw new InvalidPaymentException(
                    "Un paiement complété ne peut pas repasser à PENDING.");
        }

        payment.setStatus(request.getStatus());
        Payment saved = paymentRepository.save(payment);

        // Publier l'événement Kafka selon le nouveau statut
        String eventType = switch (saved.getStatus()) {
            case COMPLETED -> "PAYMENT_COMPLETED";
            case FAILED    -> "PAYMENT_FAILED";
            case REFUNDED  -> "PAYMENT_REFUNDED";
            default        -> null;
        };

        if (eventType != null) {
            PaymentEvent event = new PaymentEvent(
                    saved.getId(),
                    eventType,
                    saved.getContractId(),
                    saved.getPropertyId(),
                    saved.getAmount(),
                    saved.getStatus(),
                    saved.getMethod(),
                    saved.getPaymentDate()
            );
            kafkaTemplate.send(PAYMENT_TOPIC, saved.getId().toString(), event);
            log.info("[PaymentService] Événement publié → type={}, paymentId={}", eventType, saved.getId());
        }

        return toResponse(saved);
    }


    public void delete(Long id) {
        Payment payment = findOrThrow(id);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new InvalidPaymentException(
                    "Un paiement complété ne peut pas être supprimé.");
        }

        paymentRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public Double getTotalByContract(String contractId) {
        Double total = paymentRepository.getTotalPaidByContract(contractId);
        return total != null ? total : 0.0;
    }


    @Transactional(readOnly = true)
    public Double getTotalByProperty(Long propertyId) {
        Double total = paymentRepository.getTotalPaidByProperty(propertyId);
        return total != null ? total : 0.0;
    }


    @Transactional(readOnly = true)
    public List<PaymentResponse> search(String contractId, Long propertyId, PaymentStatus status) {
        return paymentRepository.searchPayments(contractId, propertyId, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getActivePropertiesPaymentsByStatus(PaymentStatus status) {

        List<ContractResponse> activeContracts = contractClient.getActiveContracts();

        List<String> contractIds = activeContracts.stream()
                .map(ContractResponse::getId)
                .toList();

        return paymentRepository.findByContractIdInAndStatus(contractIds, status)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .contractId(p.getContractId())
                .propertyId(p.getPropertyId())
                .amount(p.getAmount())
                .status(p.getStatus())
                .method(p.getMethod())
                .paymentDate(p.getPaymentDate())
                .description(p.getDescription())
                .build();
    }
}