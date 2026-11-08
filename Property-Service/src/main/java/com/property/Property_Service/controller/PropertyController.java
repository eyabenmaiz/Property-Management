package com.property.Property_Service.controller;

import com.property.Property_Service.dto.*;
import com.property.Property_Service.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Tag(name = "Property", description = "API de gestion des propriétés immobilières")
public class PropertyController {

    private final PropertyService service;


    @Operation(
            summary = "Créer une propriété",
            description = "Crée une nouvelle propriété immobilière"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriété créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PostMapping
    public PropertyResponse create(@RequestBody PropertyRequest request) {
        return service.create(request);
    }


    @Operation(
            summary = "Lister toutes les propriétés",
            description = "Retourne la liste complète des propriétés"
    )
    @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    @GetMapping
    public List<PropertyResponse> getAll() {
        return service.getAll();
    }


    @Operation(
            summary = "Récupérer une propriété par ID",
            description = "Retourne une propriété selon son identifiant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriété trouvée"),
            @ApiResponse(responseCode = "404", description = "Propriété non trouvée")
    })
    @GetMapping("/{id}")
    public PropertyResponse getById(
            @Parameter(description = "ID de la propriété", required = true)
            @PathVariable Long id) {
        return service.getById(id);
    }


    @Operation(
            summary = "Modifier une propriété",
            description = "Met à jour les informations d'une propriété existante"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriété mise à jour"),
            @ApiResponse(responseCode = "404", description = "Propriété non trouvée"),
            @ApiResponse(responseCode = "400", description = "Données invalides")
    })
    @PutMapping("/{id}")
    public PropertyResponse update(
            @Parameter(description = "ID de la propriété", required = true)
            @PathVariable Long id,
            @RequestBody PropertyRequest request) {
        return service.update(id, request);
    }


    @Operation(
            summary = "Supprimer une propriété",
            description = "Supprime une propriété selon son identifiant"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Propriété supprimée"),
            @ApiResponse(responseCode = "404", description = "Propriété non trouvée")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @Parameter(description = "ID de la propriété", required = true)
            @PathVariable Long id) {
        service.delete(id);
    }


    @Operation(
            summary = "Modifier la disponibilité",
            description = "Met à jour le statut de disponibilité d'une propriété"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilité mise à jour"),
            @ApiResponse(responseCode = "404", description = "Propriété non trouvée")
    })
    @PutMapping("/{id}/availability")
    public PropertyResponse updateAvailability(
            @Parameter(description = "ID de la propriété", required = true)
            @PathVariable Long id,
            @RequestBody AvailabilityRequest request) {
        return service.updateAvailability(id, request);
    }


    @Operation(
            summary = "Recherche avancée de propriétés",
            description = "Recherche avec filtres combinés : mot-clé, chambres, surface, prix, disponibilité. Tous les paramètres sont optionnels."
    )
    @ApiResponse(responseCode = "200", description = "Résultats de recherche retournés")
    @GetMapping("/search")
    public List<PropertyResponse> search(
            @Parameter(description = "Mot-clé dans l'adresse ou le type")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Nombre de chambres exact")
            @RequestParam(required = false) Integer rooms,

            @Parameter(description = "Surface minimale en m²")
            @RequestParam(required = false) Double minSurface,

            @Parameter(description = "Surface maximale en m²")
            @RequestParam(required = false) Double maxSurface,

            @Parameter(description = "Prix minimum du loyer")
            @RequestParam(required = false) Double minPrice,

            @Parameter(description = "Prix maximum du loyer")
            @RequestParam(required = false) Double maxPrice,

            @Parameter(description = "Disponibilité : true = disponible, false = non disponible")
            @RequestParam(required = false) Boolean available
    ) {
        return service.search(keyword, rooms, minSurface, maxSurface, minPrice, maxPrice, available);
    }


    @Operation(
            summary = "Nombre de propriétés disponibles",
            description = "Retourne le nombre total de propriétés disponibles"
    )
    @ApiResponse(responseCode = "200", description = "Comptage effectué")
    @GetMapping("/stats/available")
    public long countAvailable() {
        return service.countAvailableProperties();
    }


    @Operation(
            summary = "Nombre de propriétés non disponibles",
            description = "Retourne le nombre total de propriétés non disponibles"
    )
    @ApiResponse(responseCode = "200", description = "Comptage effectué")
    @GetMapping("/stats/unavailable")
    public long countUnavailable() {
        return service.countUnavailableProperties();
    }
}