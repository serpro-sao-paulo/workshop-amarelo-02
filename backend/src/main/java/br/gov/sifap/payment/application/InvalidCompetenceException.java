package br.gov.sifap.payment.application;

/**
 * Competência inválida (formato ou mês fora de 1–12) — REQ-PAY-008.
 * Mapeada para HTTP 400 pelo {@code PaymentExceptionHandler}.
 */
public class InvalidCompetenceException extends RuntimeException {

    public InvalidCompetenceException(String message) {
        super(message);
    }
}
