package com.payment.payment_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "property-service")
public interface PropertyClient {


    @GetMapping("/properties/{id}")
    PropertyResponse getPropertyById(@PathVariable("id") Long id);
}
