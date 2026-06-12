package com.datacorp.sifap.payment.repository;

import com.datacorp.sifap.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByCpfAndCompetence(String cpf, String competence);
    List<Payment> findByCpfOrderByCompetenceDesc(String cpf);
    boolean existsByCpfAndCompetence(String cpf, String competence);
}
