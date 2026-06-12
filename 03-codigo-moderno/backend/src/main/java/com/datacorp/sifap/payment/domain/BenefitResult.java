package com.datacorp.sifap.payment.domain;

import java.math.BigDecimal;

/**
 * Resultado do calculo do beneficio (equivalente aos campos gravados em
 * PAGAMENTO por CALCBENF).
 */
public record BenefitResult(
        BigDecimal vlrBruto,
        BigDecimal vlrDesconto,
        BigDecimal vlrLiquido,
        BigDecimal vlr13,
        BigDecimal vlrAbono,
        char tipoPgto) {
}
