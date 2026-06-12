package com.datacorp.sifap.payment.domain;

import com.datacorp.sifap.shared.MoneyMath;

import java.math.BigDecimal;

/**
 * Correcao retroativa por IPCA, traducao de CALCCORR.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L144-L189}.
 * <br>Regra: BR-008. REQ relacionado em Open Questions (MYS-008).
 *
 * <p>Fidelidade: a subrotina {@code CALC-INDICE-ACUM} multiplica o indice de
 * UM unico mes (o mes da competencia do pagamento). {@code VLR-CORR =
 * VLR-BRUTO * IND-ACUM} truncado; a correcao so e aplicada quando a diferenca e
 * positiva. Anos fora de 2010-2012 produzem fator 1,0 (correcao zero).
 */
public class CorrectionCalculator {

    public CorrectionResult calculate(BigDecimal vlrBruto, int competencia) {
        int ano = competencia / 100;
        int mes = competencia - (ano * 100);

        BigDecimal indAcum = BigDecimal.ONE;
        BigDecimal idx = IpcaTable.monthlyIndex(ano, mes);
        if (idx != null) {
            indAcum = indAcum.multiply(BigDecimal.ONE.add(idx));
        }

        BigDecimal corrigido = MoneyMath.trunc2(vlrBruto.multiply(indAcum));
        BigDecimal diff = corrigido.subtract(vlrBruto);
        boolean applied = diff.compareTo(BigDecimal.ZERO) > 0;
        return new CorrectionResult(corrigido, diff, applied);
    }
}
