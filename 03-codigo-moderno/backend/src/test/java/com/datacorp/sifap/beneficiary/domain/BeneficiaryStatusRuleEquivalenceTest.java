package com.datacorp.sifap.beneficiary.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link BeneficiaryStatusRule} vs. CADBENEF.
 * source_legacy: CADBENEF.NSN#L155-L169 (BR-016).
 *
 * <p>Branches: inclusao -&gt; 'A'; idade &gt; 75 -&gt; 'S' (boundary em 75).
 */
class BeneficiaryStatusRuleEquivalenceTest {

    private final BeneficiaryStatusRule rule = new BeneficiaryStatusRule();

    @ParameterizedTest
    @CsvSource({
        // dtNascimento, anoAtual, statusEsperado
        "19800101, 2025, A",  // idade 45 -> ativo
        "19500101, 2025, A",  // idade 75 (boundary, NAO > 75) -> ativo
        "19490101, 2025, S",  // idade 76 -> suspenso
        "19450101, 2025, S"   // idade 80 -> suspenso
    })
    void should_match_natural_status(int dtNascimento, int anoAtual, char expected) {
        assertThat(rule.initialStatus(dtNascimento, anoAtual)).isEqualTo(expected);
    }
}
