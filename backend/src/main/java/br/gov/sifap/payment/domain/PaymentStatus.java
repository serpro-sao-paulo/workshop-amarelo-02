package br.gov.sifap.payment.domain;

/**
 * Estado de um pagamento gerado no ciclo mensal.
 *
 * <p>Decisão de modernização (MYS-001): o legado usava o código 'P' de forma
 * ambígua. Aqui o estado inicial é explícito como {@link #GENERATED}, eliminando
 * a ambiguidade. REQ-PAY-010 ([GREENFIELD]).
 */
public enum PaymentStatus {
    /** Pagamento recém-gerado pelo ciclo, ainda não liberado/pago. */
    GENERATED,
    /** Pagamento cancelado antes da liberação. */
    CANCELLED,
    /** Pagamento liberado para crédito. */
    RELEASED,
    /** Pagamento efetivamente pago ao beneficiário. */
    PAID
}
