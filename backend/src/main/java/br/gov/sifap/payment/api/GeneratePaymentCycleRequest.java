package br.gov.sifap.payment.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Corpo da requisição de geração de ciclo (REQ-PAY-001).
 *
 * @param competence competência no formato {@code AAAAMM}
 */
public record GeneratePaymentCycleRequest(
        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "competence deve ter o formato AAAAMM")
        String competence) {
}
