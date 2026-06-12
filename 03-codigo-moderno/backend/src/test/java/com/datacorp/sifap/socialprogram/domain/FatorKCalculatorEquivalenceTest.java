package com.datacorp.sifap.socialprogram.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link FatorKCalculator} vs. CADPROG.
 * source_legacy: CADPROG.NSN#L86-L93 (BR-007 / MYS-002).
 *
 * <p>Valores esperados derivados de
 * {@code VLR = trunc2(BASE * (1.00 + REAJ * 0.347215))}.
 */
class FatorKCalculatorEquivalenceTest {

    private final FatorKCalculator calc = new FatorKCalculator();

    @ParameterizedTest
    @CsvSource({
        // base,    reaj,   esperado
        "1000.00, 0.0000, 1000.00",  // fator-k = 1.0
        "1000.00, 0.1000, 1034.72",  // 1000 * 1.0347215 = 1034.7215 -> trunc 1034.72
        "500.00,  0.0500, 508.68"    // 500 * 1.01736075 = 508.680375 -> trunc 508.68
    })
    void should_match_natural_fator_k(BigDecimal base, BigDecimal reaj, BigDecimal expected) {
        assertThat(calc.adjustedBaseValue(base, reaj)).isEqualByComparingTo(expected);
    }
}
