package com.contract.ContractService.controller;

import com.contract.ContractService.dto.*;
import com.contract.ContractService.service.ContractService;
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
@RequestMapping("/contracts")
@RequiredArgsConstructor
@Tag(name = "Contract", description = "API de gestion des contrats de location")
public class ContractController {

    private final ContractService contractService;


    @Operation(
            summary = "Créer un contrat",
            description = "Crée un nouveau contrat de location"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contrat créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public ResponseEntity<ContractResponse> createContract(
            @Valid @RequestBody ContractRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contractService.createContract(request));
    }


    @Operation(
            summary = "Lister tous les contrats",
            description = "Retourne la liste complète des contrats"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }


    @Operation(
            summary = "Récupérer un contrat par ID",
            description = "Retourne un contrat selon son identifiant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat trouvé"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContractById(
            @Parameter(description = "ID du contrat", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @Operation(
            summary = "Résilier un contrat",
            description = "Met fin à un contrat de location actif"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contrat résilié avec succès"),
            @ApiResponse(responseCode = "404", description = "Contrat non trouvé")
    })
    @PutMapping("/{id}/terminate")
    public ResponseEntity<ContractResponse> terminateContract(
            @Parameter(description = "ID du contrat", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(contractService.terminateContract(id));
    }

    @Operation(
            summary = "Propriétés bientôt disponibles",
            description = "Retourne les propriétés dont le contrat se termine dans X jours"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    @GetMapping("/available-soon")
    public ResponseEntity<List<AvailableSoonResponse>> getAvailableSoon(
            @Parameter(description = "Nombre de jours à anticiper (défaut: 7)", required = false)
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(
                contractService.getUpcomingAvailableProperties(days)
        );
    }


    @Operation(
            summary = "Lister les contrats inactifs",
            description = "Retourne tous les contrats terminés ou résiliés"
    )
    @ApiResponse(responseCode = "200", description = "Liste des inactifs récupérée")
    @GetMapping("/inactive")
    public ResponseEntity<List<ContractResponse>> getInactiveContracts() {
        return ResponseEntity.ok(contractService.getInactiveContracts());
    }


    @Operation(
            summary = "Lister les contrats actifs",
            description = "Retourne tous les contrats en cours"
    )
    @ApiResponse(responseCode = "200", description = "Liste des actifs récupérée")
    @GetMapping("/active")
    public ResponseEntity<List<ContractResponse>> getActiveContracts() {
        return ResponseEntity.ok(contractService.getActiveContracts());
    }
}