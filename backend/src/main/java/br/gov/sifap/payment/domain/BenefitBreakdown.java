package br.gov.sifap.payment.domain;

import java.math.BigDecimal;

/**
 * Decomposição do valor de um benefício calculado para uma competência.
 *
 * <p>Todos os valores em {@link BigDecimal} com escala 2 e truncamento
 * ({@code RoundingMode.DOWN}), espelhando o comportamento do legado (BR-004).
 *
 * @param grossAmount    valor bruto calculado
 * @param discountAmount descontos aplicados
 * @param netAmount      valor líquido (bruto - desconto), nunca negativo
 * @param bonusAmount    abono (0 no fluxo mensal regular)
 */
public record BenefitBreakdown(
        BigDecimal grossAmount,
        BigDecimal discountAmount,
        BigDecimal netAmount,
        BigDecimal bonusAmount) {
}
