package br.gov.sifap.payment.repository;

import br.gov.sifap.payment.domain.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório de {@link Payment}. Sem {@code @Transactional} aqui — a demarcação
 * transacional pertence à camada de service (Constituição V).
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, java.util.UUID> {

    /** Idempotência do ciclo (REQ-PAY-005 / BR-005). */
    boolean existsByBeneficiaryCpfAndCompetence(String beneficiaryCpf, String competence);

    /** Pagamentos de uma competência (REQ-PAY-009). */
    List<Payment> findByCompetence(String competence);
}
