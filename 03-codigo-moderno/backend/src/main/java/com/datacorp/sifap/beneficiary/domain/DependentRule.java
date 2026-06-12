package com.datacorp.sifap.beneficiary.domain;

import java.util.Set;

/**
 * Regras de inclusao de dependentes, traduzidas de CADDEPEND.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L57-L96}
 * <br>Regra: BR-017 (limite de dependentes), validacao de parentesco e bloqueio
 * por status.
 *
 * <p>Fidelidade (atencao): o guard legado e {@code IF #NUM-DEP > 5}, ou seja,
 * rejeita apenas quando a contagem ja excede 5 — permitindo incluir o 6o
 * dependente quando a contagem corrente e 5. Isso DIVERGE de REQ-005 (que
 * rejeita ao atingir 5). O teste de equivalencia preserva o comportamento
 * legado; a divergencia esta registrada em MYS-020 / BR-017.
 */
public class DependentRule {

    private static final Set<String> PARENTESCO_VALIDO = Set.of("FI", "CO", "IR", "OU");

    /** {@code IF #NUM-DEP > 5} (CADDEPEND.NSN#L63-L66). */
    public boolean limitReached(int currentDependentCount) {
        return currentDependentCount > 5;
    }

    /** Parentesco valido: FI, CO, IR, OU (CADDEPEND.NSN#L83-L86). */
    public boolean validParentesco(String parentesco) {
        return parentesco != null && PARENTESCO_VALIDO.contains(parentesco);
    }

    /** Status 'C' (cancelado) ou 'D' (desligado) bloqueia inclusao (L57-L60). */
    public boolean blocksInclusion(char titularStatus) {
        return titularStatus == 'C' || titularStatus == 'D';
    }
}
