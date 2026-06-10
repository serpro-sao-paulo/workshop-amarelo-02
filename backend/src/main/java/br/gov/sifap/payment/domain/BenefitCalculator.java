package br.gov.sifap.payment.domain;

import br.gov.sifap.payment.spi.BeneficiarySnapshot;
import br.gov.sifap.payment.spi.ProgramParameters;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Motor de cálculo do valor bruto do benefício — fonte ÚNICA da fórmula
 * (decisão D2/research; resolve a duplicação MYS-007 do legado).
 *
 * <p>Determinístico e puro (sem I/O): alvo dos testes de equivalência ao
 * programa legado {@code CALCBENF.NSN} (REQ-PAY-002 / BR-004).
 */
public interface BenefitCalculator {

    /**
     * Valor bruto do benefício, truncado em 2 casas ({@code RoundingMode.DOWN}).
     *
     * @param beneficiary snapshot do beneficiário (região, dependentes, renda, idade)
     * @param program     parâmetros do programa (base, reajuste)
     * @param competence  competência de referência (ano usado no fator idade)
     * @return valor bruto calculado
     */
    BigDecimal calculateGross(BeneficiarySnapshot beneficiary,
                              ProgramParameters program,
                              YearMonth competence);
}
