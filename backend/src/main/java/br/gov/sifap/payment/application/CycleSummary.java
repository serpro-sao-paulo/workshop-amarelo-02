package br.gov.sifap.payment.application;

import java.math.BigDecimal;

/**
 * Resumo da execução de um ciclo de pagamento (REQ-PAY-009).
 *
 * <p>Efêmero — não persistido nesta feature. Invariante de contrato:
 * {@code processed == generated + ignored + errors} (SC-004).
 *
 * @param competence    competência processada (AAAAMM)
 * @param processed     total de beneficiários avaliados
 * @param generated     pagamentos criados
 * @param ignored       beneficiários ignorados (programa inativo/inexistente, idempotência)
 * @param errors        falhas durante o cálculo individual
 * @param totalGross    soma dos valores brutos gerados
 * @param totalDiscount soma dos descontos gerados
 * @param totalNet      soma dos valores líquidos gerados
 */
public record CycleSummary(
        String competence,
        int processed,
        int generated,
        int ignored,
        int errors,
        BigDecimal totalGross,
        BigDecimal totalDiscount,
        BigDecimal totalNet) {
}
