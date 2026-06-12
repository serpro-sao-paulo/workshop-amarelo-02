package com.datacorp.sifap.beneficiary.repository;

import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    Optional<Beneficiary> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
}
