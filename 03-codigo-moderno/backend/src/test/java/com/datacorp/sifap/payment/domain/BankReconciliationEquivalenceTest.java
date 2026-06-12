package com.datacorp.sifap.payment.domain;

import com.datacorp.sifap.payment.domain.BankReconciliation.Outcome;
import com.datacorp.sifap.payment.domain.BankReconciliation.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link BankReconciliation} vs. BATCHCON.
 * source_legacy: BATCHCON.NSN#L155-L202 (BR-009 / MYS-023 / MYS-024).
 *
 * <p>Branches: diff &gt; 0,01 (divergente) vs &lt;= (conciliado); cod retorno
 * 00/01/02/desconhecido; conversao centavos-&gt;reais.
 */
class BankReconciliationEquivalenceTest {

    private final BankReconciliation recon = new BankReconciliation();

    @ParameterizedTest
    @CsvSource({
        // sifap,    banco,    cod, outcomeEsperado, statusEsperado('-'=vazio)
        "100.00, 100.00, 00, CONCILIATED, P",
        "100.00, 100.00, 01, CONCILIATED, D",
        "100.00, 100.00, 02, CONCILIATED, E",
        "100.00, 100.00, 09, CONCILIATED, -",   // cod desconhecido: sem mudanca de status
        "100.00, 100.01, 00, CONCILIATED, P",   // diff 0.01 (boundary, NAO > 0.01): concilia
        "100.00, 100.02, 00, DIVERGENT,   -",   // diff 0.02 > tolerancia: divergente
        "100.00,  99.50, 00, DIVERGENT,   -"    // diff 0.50: divergente
    })
    void should_match_natural_reconciliation(BigDecimal sifap, BigDecimal banco,
                                             String cod, Outcome expectedOutcome,
                                             String expectedStatus) {
        Result r = recon.reconcile(sifap, banco, cod);
        assertThat(r.outcome()).isEqualTo(expectedOutcome);
        if ("-".equals(expectedStatus)) {
            assertThat(r.newStatus()).isEmpty();
        } else {
            assertThat(r.newStatus()).contains(expectedStatus.charAt(0));
        }
    }

    @Test
    void should_convert_centavos_to_reais() {
        assertThat(recon.centavosToReais(10000L)).isEqualByComparingTo("100.00");
        assertThat(recon.centavosToReais(12345L)).isEqualByComparingTo("123.45");
    }
}
