package br.gov.sifap.payment.domain;

/**
 * Tipo de pagamento gerado no ciclo.
 *
 * <p>{@link #DECIMO} (13º/abono de dezembro) está previsto mas BLOQUEADO:
 * a fórmula legada é ambígua (MYS-011/MYS-015) e depende de decisão de PO/ADR
 * antes de implementar (User Story 3 — REQ-PAY-008).
 */
public enum PaymentType {
    /** Pagamento mensal regular. */
    NORMAL,
    /** 13º / abono de dezembro — regra BLOQUEADA até decisão de negócio. */
    DECIMO
}
