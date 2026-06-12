package com.datacorp.sifap.payment.domain;

import java.math.BigDecimal;

/**
 * Tabela de fatores regionais ({@code #TAB-REG}) de CALCBENF.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L88-L117},
 * {@code #L179-L184}.
 *
 * <p>Fidelidade: a tabela legada tem 27 posicoes, mas a condicao de uso e
 * {@code IF #COD-REG >= 1 AND #COD-REG <= 25}; qualquer outro valor (inclusive
 * 26, 27 e 99) usa fator 1,0000 (MYS-018).
 */
public final class RegionalFactorTable {

    // Indices 1..25 (posicao 0 nao usada).
    private static final BigDecimal[] TAB = {
        null,
        new BigDecimal("1.3500"), new BigDecimal("1.3200"), new BigDecimal("1.3000"),
        new BigDecimal("1.2800"), new BigDecimal("1.3100"), new BigDecimal("1.4000"),
        new BigDecimal("1.3800"), new BigDecimal("1.3500"), new BigDecimal("1.3200"),
        new BigDecimal("1.3600"), new BigDecimal("1.1000"), new BigDecimal("1.1200"),
        new BigDecimal("1.0800"), new BigDecimal("1.0500"), new BigDecimal("1.0000"),
        new BigDecimal("1.0500"), new BigDecimal("1.0700"), new BigDecimal("1.0300"),
        new BigDecimal("1.1500"), new BigDecimal("1.2000"), new BigDecimal("1.1800"),
        new BigDecimal("1.2500"), new BigDecimal("1.1000"), new BigDecimal("1.2200"),
        new BigDecimal("1.3300")
    };

    private static final BigDecimal DEFAULT_FACTOR = new BigDecimal("1.0000");

    private RegionalFactorTable() {
    }

    public static BigDecimal factor(int codRegiao) {
        if (codRegiao >= 1 && codRegiao <= 25) {
            return TAB[codRegiao];
        }
        return DEFAULT_FACTOR;
    }
}
