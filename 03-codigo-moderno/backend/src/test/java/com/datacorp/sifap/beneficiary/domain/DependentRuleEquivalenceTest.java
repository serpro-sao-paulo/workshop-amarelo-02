package com.datacorp.sifap.beneficiary.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link DependentRule} vs. CADDEPEND.
 * source_legacy: CADDEPEND.NSN#L57-L96.
 *
 * <p>Branches: limite ({@code > 5}), parentesco valido/invalido, status
 * bloqueante ('C'/'D').
 */
class DependentRuleEquivalenceTest {

    private final DependentRule rule = new DependentRule();

    @ParameterizedTest
    @CsvSource({
        // contagemAtual, limiteAtingido  (legado: IF #NUM-DEP > 5)
        "0, false",
        "4, false",
        "5, false",  // boundary: com 5 ainda permite incluir o 6o (DIVERGE de REQ-005)
        "6, true",
        "7, true"
    })
    void should_match_natural_limit(int currentCount, boolean expected) {
        assertThat(rule.limitReached(currentCount)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"FI", "CO", "IR", "OU"})
    void should_accept_valid_parentesco(String parentesco) {
        assertThat(rule.validParentesco(parentesco)).isTrue();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"XX", "", "fi"})
    void should_reject_invalid_parentesco(String parentesco) {
        assertThat(rule.validParentesco(parentesco)).isFalse();
    }

    @Test
    void should_block_inclusion_for_cancelled_or_disconnected() {
        assertThat(rule.blocksInclusion('C')).isTrue();
        assertThat(rule.blocksInclusion('D')).isTrue();
        assertThat(rule.blocksInclusion('A')).isFalse();
        assertThat(rule.blocksInclusion('S')).isFalse();
    }
}
