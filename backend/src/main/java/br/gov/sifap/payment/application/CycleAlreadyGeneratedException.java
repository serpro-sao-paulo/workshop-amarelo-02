package br.gov.sifap.payment.application;

/**
 * Ciclo já totalmente gerado para a competência — idempotência (REQ-PAY-005).
 * Mapeada para HTTP 409 pelo {@code PaymentExceptionHandler}.
 */
public class CycleAlreadyGeneratedException extends RuntimeException {

    public CycleAlreadyGeneratedException(String message) {
        super(message);
    }
}
