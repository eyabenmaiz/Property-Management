package com.contract.ContractService.controller;

import com.contract.ContractService.dto.TenantRequest;
import com.contract.ContractService.dto.TenantResponse;
import com.contract.ContractService.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant", description = "API de gestion des locataires")
public class TenantController {

    private final TenantService tenantService;

    // ------------------------------------------------------------------ CREATE
    @Operation(
            summary = "Créer un locataire",
            description = "Crée un nouveau locataire"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Locataire créé avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody TenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.createTenant(request));
    }

    // ------------------------------------------------------------------ GET ALL
    @Operation(
            summary = "Lister tous les locataires",
            description = "Retourne la liste complète des locataires"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    // ------------------------------------------------------------------ GET BY ID
    @Operation(
            summary = "Récupérer un locataire par ID",
            description = "Retourne un locataire selon son identifiant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locataire trouvé"),
            @ApiResponse(responseCode = "404", description = "Locataire non trouvé")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(
            @Parameter(description = "ID du locataire", required = true)
            @PathVariable String id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    // ------------------------------------------------------------------ UPDATE
    @Operation(
            summary = "Modifier un locataire",
            description = "Met à jour les informations d'un locataire existant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locataire mis à jour"),
            @ApiResponse(responseCode = "404", description = "Locataire non trouvé"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> updateTenant(
            @Parameter(description = "ID du locataire", required = true)
            @PathVariable String id,
            @Valid @RequestBody TenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(id, request));
    }

    // ------------------------------------------------------------------ DELETE
    @Operation(
            summary = "Supprimer un locataire",
            description = "Supprime un locataire selon son identifiant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Locataire supprimé"),
            @ApiResponse(responseCode = "404", description = "Locataire non trouvé")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(
            @Parameter(description = "ID du locataire", required = true)
            @PathVariable String id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}