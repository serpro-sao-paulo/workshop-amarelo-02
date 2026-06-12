package com.datacorp.sifap.beneficiary.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link FullBeneficiaryValidator} vs. VALBENEF.
 * source_legacy: VALBENEF.NSN#L185-L279 (BR-015, BR-018 / MYS-009, MYS-021).
 */
class FullBeneficiaryValidatorEquivalenceTest {

    private final FullBeneficiaryValidator v = new FullBeneficiaryValidator();

    @ParameterizedTest
    @CsvSource({
        // cpf,          valido
        "11144477735, true",   // normal valido
        "11144477700, false",  // DV incorreto
        "11111111111, false",  // todos iguais (nao 000) -> invalido
        "00000000000, true"    // QUIRK MYS-009: todos iguais mas inicia 000 -> valido
    })
    void should_match_cpf_validation(long cpf, boolean expected) {
        assertThat(v.isCpfValid(cpf)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        // dtNasc,    anoAtual, valido
        "20000229, 2025, true",   // 2000 bissexto: 29/02 valido
        "20010229, 2025, true",   // QUIRK MYS-021: 29/02 em ano nao bissexto -> valido (Fev=29 fixo)
        "20010230, 2025, false",  // dia 30 em fevereiro -> invalido
        "18991231, 2025, false",  // ano < 1900 -> invalido
        "20251301, 2025, false",  // mes 13 -> invalido
        "19800115, 2025, true"    // data comum valida
    })
    void should_match_date_validation(int dtNasc, int anoAtual, boolean expected) {
        assertThat(v.isDateValid(dtNasc, anoAtual)).isEqualTo(expected);
    }

    @Test
    void should_match_name_validation() {
        assertThat(v.isNameValid("JOSE SILVA")).isTrue();
        assertThat(v.isNameValid("JOSE")).isFalse();   // sem espaco
        assertThat(v.isNameValid(" SILVA")).isFalse(); // espaco em posicao 1 -> invalido
        assertThat(v.isNameValid("")).isFalse();
        assertThat(v.isNameValid(null)).isFalse();
    }

    @Test
    void should_match_uf_and_status_validation() {
        assertThat(v.isUfValid("SP")).isTrue();
        assertThat(v.isUfValid("XX")).isFalse();
        assertThat(v.isUfValid("")).isTrue(); // branco e aceito (UF opcional)
        assertThat(v.isStatusValid('A')).isTrue();
        assertThat(v.isStatusValid('Z')).isFalse();
    }
}
