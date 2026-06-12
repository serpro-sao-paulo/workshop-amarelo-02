package com.datacorp.sifap.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Aritmetica monetaria equivalente ao padrao mainframe do SIFAP legado.
 *
 * <p>No Natural, o truncamento para 2 casas e feito com o idioma
 * {@code COMPUTE #VLR-TEMP = X * 100} (campo inteiro N11, descarta a fracao)
 * seguido de {@code COMPUTE #VLR = #VLR-TEMP / 100}. Isso equivale a truncar em
 * direcao a zero ({@link RoundingMode#DOWN}), e NAO a arredondar.
 */
public final class MoneyMath {

    private MoneyMath() {
    }

    /** Trunca (nao arredonda) para 2 casas decimais, como o legado. */
    public static BigDecimal trunc2(BigDecimal value) {
        return value.setScale(2, RoundingMode.DOWN);
    }
}
