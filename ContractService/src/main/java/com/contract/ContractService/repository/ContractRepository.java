package com.contract.ContractService.repository;

import com.contract.ContractService.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, String> {

    boolean existsByPropertyIdAndActiveTrue(Long propertyId);

    List<Contract> findByActiveTrueAndEndDateBefore(LocalDate date);

    List<Contract> findByActiveTrueOrderByEndDateAsc();


    List<Contract> findByActiveTrueAndEndDateBetween(
            LocalDate start,
            LocalDate end
    );

    List<Contract> findByActiveTrue();

    List<Contract> findByActiveFalse();
}