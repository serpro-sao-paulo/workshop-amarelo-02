package br.gov.sifap.payment.spi;

import java.math.BigDecimal;

/**
 * Parâmetros somente-leitura de um programa social, fornecidos pelo contexto
 * Social Program. Não é entidade deste contexto (ACL in-process).
 *
 * @param code             identificação do programa
 * @param active           programa ativo (REQ-PAY-004)
 * @param baseValue        valor base do cálculo (REQ-PAY-002)
 * @param adjustmentFactor fator de reajuste (REQ-PAY-002)
 */
public record ProgramParameters(
        int code,
        boolean active,
        BigDecimal baseValue,
        BigDecimal adjustmentFactor) {
}
