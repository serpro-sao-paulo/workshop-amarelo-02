package com.datacorp.sifap.payment.domain;

import java.math.BigDecimal;

/**
 * Resultado da correcao retroativa (CALCCORR).
 *
 * @param correctedValue valor corrigido (bruto * indice acumulado, truncado)
 * @param difference     diferenca em relacao ao original
 * @param applied        true somente se {@code difference > 0} (legado so grava nesse caso)
 */
public record CorrectionResult(BigDecimal correctedValue, BigDecimal difference, boolean applied) {
}
