package com.datacorp.sifap.payment.domain;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Tabela IPCA mensal de CALCCORR.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L54-L96}.
 *
 * <p>Fidelidade (MYS-008): a tabela legada carrega apenas 2010, 2011 e 2012.
 * Competencias de anos fora desse intervalo nao encontram indice e resultam em
 * fator 1,0 (correcao zero).
 */
public final class IpcaTable {

    // indice[ano][mes 1..12]
    private static final Map<Integer, BigDecimal[]> IPCA = Map.of(
        2010, arr("0.0075", "0.0078", "0.0052", "0.0057", "0.0043", "0.0000",
                  "0.0001", "0.0004", "0.0045", "0.0075", "0.0083", "0.0063"),
        2011, arr("0.0083", "0.0080", "0.0079", "0.0077", "0.0047", "0.0015",
                  "0.0016", "0.0037", "0.0053", "0.0043", "0.0052", "0.0050"),
        2012, arr("0.0056", "0.0045", "0.0021", "0.0064", "0.0036", "0.0008",
                  "0.0043", "0.0041", "0.0054", "0.0059", "0.0060", "0.0079")
    );

    private IpcaTable() {
    }

    private static BigDecimal[] arr(String... v) {
        BigDecimal[] a = new BigDecimal[12];
        for (int i = 0; i < 12; i++) {
            a[i] = new BigDecimal(v[i]);
        }
        return a;
    }

    /** Indice do mes (1..12); {@code null} se o ano nao esta na tabela. */
    public static BigDecimal monthlyIndex(int ano, int mes) {
        BigDecimal[] year = IPCA.get(ano);
        if (year == null || mes < 1 || mes > 12) {
            return null;
        }
        return year[mes - 1];
    }
}
