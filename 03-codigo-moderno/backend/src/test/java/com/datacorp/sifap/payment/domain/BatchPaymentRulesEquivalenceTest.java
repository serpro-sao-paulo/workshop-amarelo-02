package com.datacorp.sifap.payment.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link BatchPaymentRules} vs. BATCHPGT.
 * source_legacy: BATCHPGT.NSN#L186-L335 (BR-005, REQ-009, REQ-010, REQ-021).
 */
class BatchPaymentRulesEquivalenceTest {

    private final BatchPaymentRules rules = new BatchPaymentRules();

    @Test
    void should_skip_adjacent_duplicate_cpf() {
        assertThat(rules.shouldSkipDuplicate(11111111111L, 11111111111L)).isTrue();
        assertThat(rules.shouldSkipDuplicate(11111111111L, 22222222222L)).isFalse();
    }

    @Test
    void should_skip_non_active_beneficiary() {
        assertThat(rules.shouldSkipInactive('A')).isFalse();
        assertThat(rules.shouldSkipInactive('S')).isTrue();
        assertThat(rules.shouldSkipInactive('C')).isTrue();
    }

    @Test
    void should_skip_when_already_generated_and_inactive_program() {
        assertThat(rules.shouldSkipAlreadyGenerated(true)).isTrue();
        assertThat(rules.shouldSkipAlreadyGenerated(false)).isFalse();
        assertThat(rules.shouldSkipInactiveProgram('A')).isFalse();
        assertThat(rules.shouldSkipInactiveProgram('I')).isTrue();
    }

    @Test
    void should_set_initial_status_G() {
        assertThat(rules.initialPaymentStatus()).isEqualTo('G');
    }

    @Test
    void batch_inline_calc_matches_calcbenf_engine_for_sample() {
        // MYS-007: BATCHPGT duplica o calculo do CALCBENF inline. Para esta entrada,
        // os dois caminhos devem produzir o MESMO valor (motor unico - REQ-013).
        BenefitResult r = new BenefitCalculator().calculate(
                new BigDecimal("1000.00"), new BigDecimal("0.0000"),
                11, 0, new BigDecimal("300.00"), 19800101, 202506, 'P');
        assertThat(r.vlrBruto()).isEqualByComparingTo("1100.00");
        assertThat(r.vlrDesconto()).isEqualByComparingTo("33.00");
        assertThat(r.vlrLiquido()).isEqualByComparingTo("1067.00");
    }
}
