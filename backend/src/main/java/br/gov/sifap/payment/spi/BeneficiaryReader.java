package br.gov.sifap.payment.spi;

import java.util.List;

/**
 * Porta de leitura para o contexto Beneficiary Management.
 *
 * <p>Implementação fornecida por aquele contexto (ou por um adaptador de teste).
 * Mantém o acoplamento entre módulos restrito a interfaces, conforme o
 * Modular Monolith (Constituição III). Troca apenas o DTO de leitura
 * {@link BeneficiarySnapshot}, sem expor entidades JPA do outro contexto.
 */
public interface BeneficiaryReader {

    /**
     * Beneficiários elegíveis (status {@code ACTIVE} na data de corte) para o ciclo
     * da competência informada (REQ-PAY-001/003).
     *
     * @param competence competência no formato {@code AAAAMM}
     * @return lista de snapshots elegíveis (nunca {@code null})
     */
    List<BeneficiarySnapshot> findActiveForCompetence(String competence);
}
