package com.datacorp.sifap.payment.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link DiscountCalculator} vs. CALCDSCT.
 * source_legacy: CALCDSCT.NSN#L57-L186 (BR-001 teto 30%, BR-003 contribuicao).
 *
 * <p>Branches: contribuicao progressiva (4 faixas), tipos de desconto
 * (J/P/I/S/A), teto 30% aplicado no loop (exceto J), vigencia (dtFim &lt; hoje
 * e dtInicio &gt; hoje).
 */
class DiscountCalculatorEquivalenceTest {

    private static final int DT_HOJE = 20250101;
    private final DiscountCalculator calc = new DiscountCalculator();

    @ParameterizedTest
    @CsvSource({
        // bruto,   contribEsperada (sem outros descontos)
        "400.00,   12.00",   // faixa 1: 3%
        "500.00,   15.00",   // boundary faixa 1 (<= 500): 3%
        "800.00,   40.00",   // faixa 2: 5%
        "1500.00, 105.00",   // faixa 3: 7%
        "5000.00, 450.00"    // faixa 4: 9%
    })
    void should_match_progressive_contribution(BigDecimal bruto, BigDecimal expected) {
        assertThat(calc.calculate(bruto, List.of(), DT_HOJE)).isEqualByComparingTo(expected);
    }

    @Test
    void should_apply_30_percent_cap_in_loop_for_non_judicial() {
        // bruto 1000 -> contrib 50; +I 20% (200)=250; +I 10% (100)=350 -> teto 300
        List<Discount> descontos = List.of(
                new Discount('I', BigDecimal.ZERO, new BigDecimal("20"), 0, 0),
                new Discount('I', BigDecimal.ZERO, new BigDecimal("10"), 0, 0));
        assertThat(calc.calculate(new BigDecimal("1000.00"), descontos, DT_HOJE))
                .isEqualByComparingTo("300.00");
    }

    @Test
    void should_not_cap_judicial_discount() {
        // bruto 1000 -> contrib 50; +J 50% (500)=550, judicial sem teto
        List<Discount> descontos = List.of(
                new Discount('J', BigDecimal.ZERO, new BigDecimal("50"), 0, 0));
        assertThat(calc.calculate(new BigDecimal("1000.00"), descontos, DT_HOJE))
                .isEqualByComparingTo("550.00");
    }

    @Test
    void should_apply_sindical_fixed_one_percent() {
        // bruto 1000 -> contrib 50; +S (1% = 10) = 60
        List<Discount> descontos = List.of(
                new Discount('S', BigDecimal.ZERO, BigDecimal.ZERO, 0, 0));
        assertThat(calc.calculate(new BigDecimal("1000.00"), descontos, DT_HOJE))
                .isEqualByComparingTo("60.00");
    }

    @Test
    void should_ignore_out_of_validity_discounts() {
        // dtFim 20200101 < hoje -> item ignorado; total = contrib 50
        List<Discount> descontos = List.of(
                new Discount('I', BigDecimal.ZERO, new BigDecimal("20"), 0, 20200101));
        assertThat(calc.calculate(new BigDecimal("1000.00"), descontos, DT_HOJE))
                .isEqualByComparingTo("50.00");
    }
}
