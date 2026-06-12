package com.datacorp.sifap.beneficiary.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link CpfMask} vs. subrotina MASCARA-CPF.
 * source_legacy: CONSBENF.NSN#L171-L189 (MYS-005).
 *
 * <p>Branches: CPF completo (&gt;= 10^10) -&gt; oculta primeiros, mostra ultimos;
 * CPF com zero a esquerda (&lt; 10^10) -&gt; expoe os 3 primeiros (inconsistencia
 * legada preservada). Estes testes verificam o COMPORTAMENTO LEGADO, nao o
 * desejado (REQ-006/LGPD).
 */
class CpfMaskEquivalenceTest {

    private final CpfMask mask = new CpfMask();

    @ParameterizedTest
    @CsvSource({
        // cpf,          mascaraEsperada
        "11144477735, ***.***.777-35",  // completo: mostra ultimos
        "12345678909, ***.***.789-09",  // completo
        "1144477735,  011.***.***-**"   // < 10^10: QUIRK expoe os 3 primeiros ('011')
    })
    void should_match_natural_mask(long cpf, String expected) {
        assertThat(mask.maskLegacy(cpf)).isEqualTo(expected);
    }
}
