package com.contract.ContractService.client;

import com.contract.ContractService.dto.AvailabilityRequest;
import com.contract.ContractService.dto.PropertyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "Property-Service")
public interface PropertyClient {

    @GetMapping("/properties/{id}")
    PropertyResponse getPropertyById(@PathVariable("id") Long id);

    @PutMapping("/properties/{id}/availability")
    PropertyResponse updateAvailability(
            @PathVariable("id") Long id,
            @RequestBody AvailabilityRequest request
    );
}