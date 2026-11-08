package com.payment.payment_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "contract-service")
public interface ContractClient {

    // Récupérer un contrat par ID
    @GetMapping("/contracts/{id}")
    ContractResponse getContractById(@PathVariable("id") String id);


    @GetMapping("/contracts/active")
    List<ContractResponse> getActiveContracts();
}