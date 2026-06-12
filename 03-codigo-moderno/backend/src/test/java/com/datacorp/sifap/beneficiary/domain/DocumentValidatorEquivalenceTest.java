package com.datacorp.sifap.beneficiary.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link DocumentValidator} vs. VALDOCS.
 * source_legacy: VALDOCS.NSN#L103-L182 (BR-010 / MYS-003 — backdoor).
 */
class DocumentValidatorEquivalenceTest {

    private final DocumentValidator v = new DocumentValidator();

    @Test
    void should_validate_cpf_rejecting_zero() {
        assertThat(v.isCpfValid(0L)).isFalse();
        assertThat(v.isCpfValid(11144477735L)).isTrue();
        assertThat(v.isCpfValid(11144477700L)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "12345, true",
        "ABCDE, true",
        "1234, false",  // < 5 caracteres
        "'', false"
    })
    void should_validate_rg_min_length(String rg, boolean expected) {
        assertThat(v.isRgValid(rg)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        // cpf,           especial
        "00012345678, true",   // prefixo 000
        "99912345678, true",   // prefixo 999
        "10012345678, true",   // prefixo 100
        "11144477735, false"   // prefixo 111
    })
    void should_detect_special_prefix(long cpf, boolean expected) {
        assertThat(v.isSpecialDocument(cpf)).isEqualTo(expected);
    }

    @Test
    void backdoor_forces_valid_even_with_bad_cpf_and_rg() {
        // MYS-003: prefixo especial (999) zera erros e forca documento valido
        assertThat(v.isValidLegacy(99900000000L, " ")).isTrue();
        // sem prefixo especial: depende de CPF valido + RG valido
        assertThat(v.isValidLegacy(11144477735L, "12345")).isTrue();
        assertThat(v.isValidLegacy(11144477700L, "12345")).isFalse(); // CPF invalido
    }
}
