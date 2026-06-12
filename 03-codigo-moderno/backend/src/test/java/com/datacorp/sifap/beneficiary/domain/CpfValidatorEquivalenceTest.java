package com.datacorp.sifap.beneficiary.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link CpfValidator} vs. subrotina VALIDA-CPF.
 * source_legacy: CADBENEF.NSN#L224-L269.
 *
 * <p>Branches identificados no Natural:
 * <ul>
 *   <li>DV1 com resto &lt; 2 (DV=0) vs resto &gt;= 2 (11-resto)</li>
 *   <li>DV1 confere / nao confere (ESCAPE ROUTINE)</li>
 *   <li>DV2 com resto &lt; 2 vs &gt;= 2</li>
 *   <li>DV2 confere / nao confere</li>
 * </ul>
 */
class CpfValidatorEquivalenceTest {

    private final CpfValidator validator = new CpfValidator();

    @ParameterizedTest
    @CsvSource({
        // cpf,           esperado, // descricao do branch
        "11144477735, true",   // happy path: DV1=3 (resto>=2), DV2=5 (resto>=2)
        "12345678909, true",   // DV1=0 (resto<2), DV2=9
        "11144477700, false",  // DV1 nao confere
        "11144477736, false",  // DV2 nao confere (DV1 ok, ultimo digito errado)
        "11111111111, true",   // QUIRK legado: digitos repetidos passam no mod-11 (rejeicao e do VALBENEF)
        "00000000000, true"    // QUIRK: a nivel de subrotina, zeros passam (CADBENEF rejeita CPF=0 antes, REQ-002)
    })
    void should_match_natural_mod11(long cpf, boolean expected) {
        assertThat(validator.isValid(cpf)).isEqualTo(expected);
    }

    @Test
    void should_reject_out_of_range_values() {
        // Boundary: valores fora de 11 digitos nao sao CPFs validos
        assertThat(validator.isValid(-1L)).isFalse();
        assertThat(validator.isValid(100_000_000_000L)).isFalse(); // 12 digitos
    }
}
