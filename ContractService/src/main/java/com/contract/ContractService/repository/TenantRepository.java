package com.contract.ContractService.repository;

import com.contract.ContractService.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
}