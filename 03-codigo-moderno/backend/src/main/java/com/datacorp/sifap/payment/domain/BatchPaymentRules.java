package com.datacorp.sifap.payment.domain;

/**
 * Regras de gating do ciclo de pagamento em lote, traducao de BATCHPGT.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L186-L235}.
 * <br>Regras: BR-005 (so ativos), idempotencia, dedup adjacente. REQ-009, REQ-010, REQ-021.
 *
 * <p>Fidelidade: o calculo monetario do BATCHPGT e identico ao do CALCBENF
 * (ver {@link BenefitCalculator}); o header diz "CHAMA CALCBENF" mas a logica e
 * duplicada inline (MYS-007). Aqui ficam apenas as decisoes de fluxo do lote.
 * O status inicial gravado e sempre {@code 'G'} (REQ-021).
 */
public class BatchPaymentRules {

    /** Dedup de registros ADJACENTES por CPF (BATCHPGT.NSN#L188-L191). */
    public boolean shouldSkipDuplicate(long cpf, long previousCpf) {
        return cpf == previousCpf;
    }

    /** So beneficiario ativo gera pagamento (BATCHPGT.NSN#L195-L198). */
    public boolean shouldSkipInactive(char beneficiaryStatus) {
        return beneficiaryStatus != 'A';
    }

    /** Idempotencia: ja existe pagamento na competencia (BATCHPGT.NSN#L202-L210). */
    public boolean shouldSkipAlreadyGenerated(boolean alreadyGeneratedInCompetence) {
        return alreadyGeneratedInCompetence;
    }

    /** Programa inativo nao gera pagamento (BATCHPGT.NSN#L227-L230). */
    public boolean shouldSkipInactiveProgram(char programStatus) {
        return programStatus != 'A';
    }

    /** Status gravado ao gerar o pagamento (BATCHPGT.NSN#L332-L335). */
    public char initialPaymentStatus() {
        return 'G';
    }
}
