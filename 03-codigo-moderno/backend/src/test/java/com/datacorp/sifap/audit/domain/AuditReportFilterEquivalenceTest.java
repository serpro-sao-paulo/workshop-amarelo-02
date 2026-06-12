package com.datacorp.sifap.audit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link AuditReportFilter} vs. RELAUDIT.
 * source_legacy: RELAUDIT.NSN#L102-L180 (BR-011 / MYS-004).
 */
class AuditReportFilterEquivalenceTest {

    private final AuditReportFilter filter = new AuditReportFilter();

    @Test
    void should_always_hide_exclusion_events() {
        // MYS-004: ACAO='EX' nunca aparece (mesmo sem filtros)
        assertThat(filter.isHiddenLegacy("EX", "", "", "", "USER", "PAGAMENTO")).isTrue();
    }

    @Test
    void should_show_non_excluded_without_filters() {
        assertThat(filter.isHiddenLegacy("IN", "", "", "", "USER", "BENEFICIARIO")).isFalse();
    }

    @Test
    void should_hide_when_action_filter_does_not_match() {
        assertThat(filter.isHiddenLegacy("IN", "AL", "", "", "USER", "BENEFICIARIO")).isTrue();
        assertThat(filter.isHiddenLegacy("AL", "AL", "", "", "USER", "BENEFICIARIO")).isFalse();
    }

    @Test
    void should_hide_when_user_or_table_filter_does_not_match() {
        assertThat(filter.isHiddenLegacy("IN", "", "JOAO", "", "USER", "BENEFICIARIO")).isTrue();
        assertThat(filter.isHiddenLegacy("IN", "", "", "PAGAMENTO", "USER", "BENEFICIARIO")).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
        "IN, INCLUSAO", "AL, ALTERACAO", "CO, CONCILIACAO",
        "CN, CONSULTA", "DV, DIVERGENCIA", "ZZ, OUTRA"
    })
    void should_match_action_description(String acao, String expected) {
        assertThat(filter.actionDescription(acao)).isEqualTo(expected);
    }
}
