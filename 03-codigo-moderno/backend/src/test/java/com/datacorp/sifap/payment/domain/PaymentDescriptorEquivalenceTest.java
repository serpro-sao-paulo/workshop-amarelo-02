package com.datacorp.sifap.payment.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link PaymentDescriptor} vs. RELPGT.
 * source_legacy: RELPGT.NSN#L116-L141 (BR-020 / MYS-025).
 */
class PaymentDescriptorEquivalenceTest {

    @ParameterizedTest
    @CsvSource({
        "G, GERADO", "P, PAGO", "C, CANCELAD", "D, DEVOLVID", "E, ESTORNAD", "Z, OUTRO"
    })
    void should_match_status_description(char status, String expected) {
        assertThat(PaymentDescriptor.statusDescription(status)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "N, NORMAL", "D, DECIMO", "T, TERCEIRO", "X, OUTRO"
    })
    void should_match_type_description(char tipo, String expected) {
        assertThat(PaymentDescriptor.typeDescription(tipo)).isEqualTo(expected);
    }
}
