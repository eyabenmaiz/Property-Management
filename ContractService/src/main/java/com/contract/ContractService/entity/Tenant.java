package com.contract.ContractService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tenants")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private String cin;

    @NotBlank
    private String phone;
}