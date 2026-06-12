package com.datacorp.sifap.payment.domain;

import com.datacorp.sifap.shared.MoneyMath;

import java.math.BigDecimal;

/**
 * Regras de consolidacao do relatorio mensal, traducao de BATCHREL.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L105-L169}.
 * <br>Regras: macro-regiao (1-25 -> 5 baldes), arredondamento (MYS-022),
 * balde por status (MYS-033).
 *
 * <p>Fidelidade: o mapeamento de regiao usa o ramo "demais" para 21-25 e
 * qualquer outro valor (inclui 99) -> Centro-Oeste (indice 5). O arredondamento
 * do relatorio e {@code trunc2(vlr + 0,005)} (round half-up), divergente do
 * truncamento do calculo. Status desconhecido cai no balde 1 (GERADO).
 */
public final class ReportConsolidation {

    private static final BigDecimal HALF_CENT = new BigDecimal("0.005");

    private ReportConsolidation() {
    }

    /** Indice 1..5 da macro-regiao (BATCHREL.NSN#L117-L133). */
    public static int regionBucket(int codRegiao) {
        if (codRegiao >= 1 && codRegiao <= 5) {
            return 1; // NORTE
        }
        if (codRegiao >= 6 && codRegiao <= 10) {
            return 2; // NORDESTE
        }
        if (codRegiao >= 11 && codRegiao <= 15) {
            return 3; // SUDESTE
        }
        if (codRegiao >= 16 && codRegiao <= 20) {
            return 4; // SUL
        }
        return 5; // CENTRO-OESTE (21-25 e qualquer outro, inclui 99)
    }

    /** Indice 1..5 do balde de status; desconhecido -> 1 GERADO (MYS-033). */
    public static int statusBucket(char statusPgto) {
        return switch (statusPgto) {
            case 'G' -> 1;
            case 'P' -> 2;
            case 'C' -> 3;
            case 'D' -> 4;
            case 'E' -> 5;
            default -> 1; // NONE -> GERADO
        };
    }

    /** Arredondamento do relatorio: trunc2(vlr + 0,005) (BATCHREL.NSN#L136-L140). */
    public static BigDecimal reportRounding(BigDecimal vlrBruto) {
        return MoneyMath.trunc2(vlrBruto.add(HALF_CENT));
    }
}
