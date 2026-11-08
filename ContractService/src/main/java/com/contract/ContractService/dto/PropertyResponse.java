package com.contract.ContractService.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {

    private Long id;
    private String type;
    private String address;
    private Double rentPrice;
    private Boolean available;
    private Description description;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Description {
        private Integer rooms;
        private Double surface;
        private String criteria;
    }

    public boolean isAvailable() {
        return Boolean.TRUE.equals(available);
    }
}