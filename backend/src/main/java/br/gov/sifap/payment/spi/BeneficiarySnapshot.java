package br.gov.sifap.payment.spi;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Visão somente-leitura de um beneficiário, fornecida pelo contexto
 * Beneficiary Management. Não é entidade deste contexto (ACL in-process).
 *
 * @param cpf             identificação
 * @param status          elegibilidade (REQ-PAY-003)
 * @param regionCode      código da região para fator regional (REQ-PAY-002)
 * @param dependentsCount número de dependentes para fator familiar (REQ-PAY-002)
 * @param familyIncome    renda familiar para fator renda (REQ-PAY-002)
 * @param birthDate       data de nascimento para fator idade (REQ-PAY-002)
 * @param programCode     programa social ao qual está vinculado
 */
public record BeneficiarySnapshot(
        String cpf,
        BeneficiaryStatus status,
        int regionCode,
        int dependentsCount,
        BigDecimal familyIncome,
        LocalDate birthDate,
        int programCode) {
}
