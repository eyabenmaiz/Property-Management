package com.payment.payment_service.controller;

import com.payment.payment_service.dto.*;
import com.payment.payment_service.entity.PaymentStatus;
import com.payment.payment_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Gestion des paiements de loyer")
public class PaymentController {

    private final PaymentService paymentService;


    @Operation(
            summary = "Créer un paiement",
            description = "Enregistre un nouveau paiement de loyer"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paiement créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.create(request));
    }


    @Operation(
            summary = "Lister tous les paiements",
            description = "Retourne la liste complète des paiements"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAll() {
        return ResponseEntity.ok(paymentService.getAll());
    }


    @Operation(
            summary = "Obtenir un paiement par ID",
            description = "Retourne un paiement selon son identifiant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paiement trouvé"),
            @ApiResponse(responseCode = "404", description = "Paiement non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(
            @Parameter(description = "ID du paiement", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getById(id));
    }

    //GET BY CONTRACT
    @Operation(
            summary = "Lister les paiements d'un contrat",
            description = "Retourne tous les paiements liés à un contrat"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    })
    @GetMapping("/contract/{contractId}")
    public ResponseEntity<List<PaymentResponse>> getByContract(
            @Parameter(description = "ID du contrat", required = true)
            @PathVariable String contractId) {
        return ResponseEntity.ok(paymentService.getByContract(contractId));
    }

    //GET BY PROPERTY
    @Operation(
            summary = "Lister les paiements d'une propriété",
            description = "Retourne tous les paiements liés à une propriété"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
            @ApiResponse(responseCode = "404", description = "Propriété non trouvée")
    })
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<PaymentResponse>> getByProperty(
            @Parameter(description = "ID de la propriété", required = true)
            @PathVariable Long propertyId) {
        return ResponseEntity.ok(paymentService.getByProperty(propertyId));
    }

    // GET BY STATUS
    @Operation(
            summary = "Lister les paiements par statut",
            description = "Retourne tous les paiements selon leur statut (PENDING, PAID, LATE...)"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getByStatus(
            @Parameter(description = "Statut du paiement", required = true)
            @PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getByStatus(status));
    }

    // TOTAL BY CONTRACT
    @Operation(
            summary = "Total payé pour un contrat",
            description = "Calcule le montant total des paiements d'un contrat"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total calculé avec succès"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    })
    @GetMapping("/contract/{contractId}/total")
    public ResponseEntity<Double> getTotalByContract(
            @Parameter(description = "ID du contrat", required = true)
            @PathVariable String contractId) {
        return ResponseEntity.ok(paymentService.getTotalByContract(contractId));
    }

    //  UPDATE STATUS
    @Operation(
            summary = "Modifier le statut d'un paiement",
            description = "Met à jour le statut d'un paiement existant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statut mis à jour"),
            @ApiResponse(responseCode = "404", description = "Paiement non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(
            @Parameter(description = "ID du paiement", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PaymentStatusRequest request) {
        return ResponseEntity.ok(paymentService.updateStatus(id, request));
    }

    //DELETE
    @Operation(
            summary = "Supprimer un paiement",
            description = "Supprime un paiement selon son identifiant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paiement supprimé"),
            @ApiResponse(responseCode = "404", description = "Paiement non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID du paiement", required = true)
            @PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ ACTIVE PROPERTIES BY STATUS
    @Operation(
            summary = "Paiements des propriétés actives par statut",
            description = "Retourne les paiements des propriétés actives filtrés par statut"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping("/properties/active/{status}")
    public ResponseEntity<List<PaymentResponse>> getActivePropertiesPaymentsByStatus(
            @Parameter(description = "Statut du paiement", required = true)
            @PathVariable PaymentStatus status) {
        return ResponseEntity.ok(
                paymentService.getActivePropertiesPaymentsByStatus(status)
        );
    }
}