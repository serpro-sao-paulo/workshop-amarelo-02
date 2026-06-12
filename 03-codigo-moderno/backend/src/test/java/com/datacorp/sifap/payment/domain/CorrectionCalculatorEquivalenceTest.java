package com.datacorp.sifap.payment.domain;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes de equivalencia para {@link CorrectionCalculator} vs. CALCCORR.
 * source_legacy: CALCCORR.NSN#L144-L189 (BR-008 / MYS-008).
 *
 * <p>Branches: ano presente na tabela IPCA vs ausente (fator 1,0);
 * diferenca &gt; 0 (aplica) vs &lt;= 0 (nao aplica). Valor original fixo em
 * R$ 1000,00.
 */
class CorrectionCalculatorEquivalenceTest {

    private static final BigDecimal ORIG = new BigDecimal("1000.00");
    private final CorrectionCalculator calc = new CorrectionCalculator();

    @ParameterizedTest
    @CsvSource({
        // competencia, corrigidoEsperado, aplicadoEsperado
        "201001, 1007.50, true",   // IPCA jan/2010 = 0.0075
        "201003, 1005.20, true",   // IPCA mar/2010 = 0.0052
        "201212, 1007.90, true",   // IPCA dez/2012 = 0.0079
        "201006, 1000.00, false",  // IPCA jun/2010 = 0.0000 -> diff 0 -> nao aplica
        "201501, 1000.00, false"   // MYS-008: ano fora da tabela -> fator 1.0 -> nao aplica
    })
    void should_match_natural_correction(int competencia, BigDecimal expectedCorr, boolean expectedApplied) {
        CorrectionResult r = calc.calculate(ORIG, competencia);
        assertThat(r.correctedValue()).isEqualByComparingTo(expectedCorr);
        assertThat(r.applied()).isEqualTo(expectedApplied);
    }

    @Test
    @Disabled("MYSTERY MYS-030: correcao Plano Verao (1989-1991, fatores 2.75 e 1.4289) "
            + "esta comentada como codigo morto em CALCCORR.NSN#L98-L111; nao executa. "
            + "Decidir se deve ser descartada na modernizacao.")
    void mystery_plano_verao_dead_code() {
        fail("Codigo morto - veja MYS-030");
    }
}
