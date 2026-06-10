package br.gov.sifap.payment.domain;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.sifap.payment.spi.BeneficiarySnapshot;
import br.gov.sifap.payment.spi.BeneficiaryStatus;
import br.gov.sifap.payment.spi.ProgramParameters;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes de equivalência ao motor legado CALCBENF.NSN (REQ-PAY-002 / BR-004).
 * Casos derivados das tabelas e faixas do programa Natural.
 */
class DefaultBenefitCalculatorTest {

    private final BenefitCalculator calculator = new DefaultBenefitCalculator();
    private static final YearMonth COMPETENCE = YearMonth.of(2026, 6);

    private BeneficiarySnapshot beneficiary(int region, int dependents,
                                            String income, int birthYear) {
        return new BeneficiarySnapshot(
                "00000000000",
                BeneficiaryStatus.ACTIVE,
                region,
                dependents,
                new BigDecimal(income),
                LocalDate.of(birthYear, 1, 1),
                10);
    }

    private ProgramParameters program(String base, String adjustment) {
        return new ProgramParameters(10, true, new BigDecimal(base), new BigDecimal(adjustment));
    }

    @Test
    @DisplayName("SP, sem dependentes, renda baixa, adulto, sem reajuste → base × 1,10")
    void simpleCase() {
        // 100 × 1.10(SP) × 1.0(0 dep) × 1.0(renda≤300) × 1.0(idade 30) × 1.0 = 110.00
        BigDecimal gross = calculator.calculateGross(
                beneficiary(11, 0, "200.00", 1996), program("100.00", "0.00"), COMPETENCE);
        assertThat(gross).isEqualByComparingTo("110.00");
    }

    @Test
    @DisplayName("Caso composto com truncamento (DOWN) — todos os fatores")
    void compositeWithTruncation() {
        // 100 × 1.40(MA) × 1.13(3 dep) × 0.70(renda≤1000) × 1.15(idade 70) × 1.05(reaj)
        //   = 133.71855 → trunca → 133.71
        BigDecimal gross = calculator.calculateGross(
                beneficiary(6, 3, "700.00", 1956), program("100.00", "0.05"), COMPETENCE);
        assertThat(gross).isEqualByComparingTo("133.71");
    }

    @Test
    @DisplayName("Região fora de 1–25 usa fator 1,0000")
    void regionOutOfRange() {
        // 100 × 1.0(região 99) × 1.0 × 1.0 × 1.0 × 1.0 = 100.00
        BigDecimal gross = calculator.calculateGross(
                beneficiary(99, 0, "200.00", 1996), program("100.00", "0.00"), COMPETENCE);
        assertThat(gross).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("Menor de 18 anos recebe fator idade 1,05")
    void minorAgeFactor() {
        // 100 × 1.10(SP) × 1.0 × 1.0 × 1.05(idade 10) × 1.0 = 115.50
        BigDecimal gross = calculator.calculateGross(
                beneficiary(11, 0, "200.00", 2016), program("100.00", "0.00"), COMPETENCE);
        assertThat(gross).isEqualByComparingTo("115.50");
    }

    @Test
    @DisplayName("Cinco ou mais dependentes: fator 1,16 + 0,02 por dependente acima de 4")
    void largeFamilyFactor() {
        // 5 dep → 1.1600 + (5-4)*0.0200 = 1.1800
        // 100 × 1.0(REF=15) × 1.18 × 1.0 × 1.0 × 1.0 = 118.00
        BigDecimal gross = calculator.calculateGross(
                beneficiary(15, 5, "200.00", 1996), program("100.00", "0.00"), COMPETENCE);
        assertThat(gross).isEqualByComparingTo("118.00");
    }

    @Test
    @DisplayName("Valor bruto nunca negativo para entradas válidas")
    void neverNegative() {
        BigDecimal gross = calculator.calculateGross(
                beneficiary(11, 0, "9000.00", 1996), program("100.00", "0.00"), COMPETENCE);
        assertThat(gross).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
