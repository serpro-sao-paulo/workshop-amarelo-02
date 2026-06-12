package com.datacorp.sifap.payment.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link ReportConsolidation} vs. BATCHREL.
 * source_legacy: BATCHREL.NSN#L117-L159 (macro-regiao, MYS-022, MYS-033).
 */
class ReportConsolidationEquivalenceTest {

    @ParameterizedTest
    @CsvSource({
        // codRegiao, bucketEsperado
        "1, 1", "5, 1",     // Norte
        "6, 2", "10, 2",    // Nordeste
        "11, 3", "15, 3",   // Sudeste
        "16, 4", "20, 4",   // Sul
        "21, 5", "25, 5",   // Centro-Oeste
        "99, 5", "0, 5"     // demais (inclui 99) -> Centro-Oeste
    })
    void should_match_region_bucket(int codRegiao, int expected) {
        assertThat(ReportConsolidation.regionBucket(codRegiao)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "G, 1", "P, 2", "C, 3", "D, 4", "E, 5",
        "X, 1"   // MYS-033: status desconhecido -> balde 1 (GERADO)
    })
    void should_match_status_bucket(char status, int expected) {
        assertThat(ReportConsolidation.statusBucket(status)).isEqualTo(expected);
    }

    @Test
    void report_rounding_is_noop_on_two_decimal_values() {
        // MYS-022: trunc2(vlr + 0.005). Em valores ja com 2 casas (N9.2) o efeito
        // e nulo, mas a operacao legada e reproduzida fielmente.
        assertThat(ReportConsolidation.reportRounding(new BigDecimal("100.00")))
                .isEqualByComparingTo("100.00");
        assertThat(ReportConsolidation.reportRounding(new BigDecimal("100.99")))
                .isEqualByComparingTo("100.99");
    }
}
