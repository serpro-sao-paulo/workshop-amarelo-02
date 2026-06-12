package com.datacorp.sifap.socialprogram.domain;

import com.datacorp.sifap.shared.MoneyMath;

import java.math.BigDecimal;

/**
 * Ajuste do valor-base do programa pelo "FATOR-K", traducao de CADPROG.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L86-L93}
 * <br>Regra: BR-007 / MYS-002.
 *
 * <p>Fidelidade: {@code #FATOR-K = 1.00 + (#FATOR-REAJ * 0.347215)} e
 * {@code #VLR-CALC = #VLR-BASE * #FATOR-K}. O resultado e gravado em campo N9.2,
 * portanto truncado para 2 casas. A constante 0,347215 nao tem origem
 * documentada (MYS-002).
 */
public class FatorKCalculator {

    private static final BigDecimal FATOR_K_CONST = new BigDecimal("0.347215");
    private static final BigDecimal ONE = new BigDecimal("1.00");

    public BigDecimal adjustedBaseValue(BigDecimal vlrBase, BigDecimal fatorReajuste) {
        BigDecimal fatorK = ONE.add(fatorReajuste.multiply(FATOR_K_CONST));
        return MoneyMath.trunc2(vlrBase.multiply(fatorK));
    }
}
