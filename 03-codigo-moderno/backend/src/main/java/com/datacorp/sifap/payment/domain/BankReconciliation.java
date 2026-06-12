package com.datacorp.sifap.payment.domain;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Conciliacao bancaria (CNAB), traducao de BATCHCON.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L155-L230}.
 * <br>Regras: BR-009 (tolerancia 0,01 / divergencia), transicoes de status
 * (00->P, 01->D, 02->E). REQ-016, REQ-017, REQ-022.
 *
 * <p>Fidelidade: a diferenca absoluta entre o liquido do SIFAP e o valor do
 * retorno; se {@code > 0,01} e divergencia (gera auditoria DV); caso contrario
 * concilia e aplica a transicao de status pelo codigo de retorno. Codigo fora
 * de 00/01/02 mantem o status anterior. A tolerancia 0,01 e o COD-BANCO fixo = 1
 * sao magic numbers (MYS-023, MYS-024).
 */
public class BankReconciliation {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");
    private static final BigDecimal CEM = new BigDecimal("100");

    public enum Outcome { CONCILIATED, DIVERGENT }

    /**
     * @param outcome    CONCILIATED ou DIVERGENT
     * @param newStatus  novo status do pagamento, presente apenas quando
     *                   conciliado E o codigo de retorno e 00/01/02
     */
    public record Result(Outcome outcome, Optional<Character> newStatus) {
    }

    /** Converte valor CNAB de centavos para reais (BATCHCON.NSN#L130-L132). */
    public BigDecimal centavosToReais(long centavos) {
        return new BigDecimal(centavos).divide(CEM);
    }

    public Result reconcile(BigDecimal vlrLiquidoSifap, BigDecimal vlrRetorno, String codRetorno) {
        BigDecimal diff = vlrLiquidoSifap.subtract(vlrRetorno).abs();
        if (diff.compareTo(TOLERANCE) > 0) {
            return new Result(Outcome.DIVERGENT, Optional.empty());
        }
        Character status = switch (codRetorno) {
            case "00" -> 'P';
            case "01" -> 'D';
            case "02" -> 'E';
            default -> null; // COD RETORNO DESCONHECIDO: status inalterado
        };
        return new Result(Outcome.CONCILIATED, Optional.ofNullable(status));
    }
}
