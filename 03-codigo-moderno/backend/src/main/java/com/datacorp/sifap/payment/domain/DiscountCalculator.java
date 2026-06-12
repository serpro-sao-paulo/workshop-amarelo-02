package com.datacorp.sifap.payment.domain;

import com.datacorp.sifap.shared.MoneyMath;

import java.math.BigDecimal;
import java.util.List;

/**
 * Motor de descontos, traducao de CALCDSCT.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L57-L186}.
 * <br>Regras: BR-001 (teto 30%), BR-003 (contribuicao progressiva). REQ-011, REQ-012.
 *
 * <p>Fidelidade: a contribuicao social (progressiva por faixa) e somada antes do
 * loop. O teto de 30% e calculado e aplicado DENTRO do loop, apos cada item nao
 * judicial (ordem dos descontos no PE afeta o resultado — MYS-014). Descontos
 * tipo 'J' (judicial) nao sofrem teto. Itens fora de vigencia sao ignorados.
 */
public class DiscountCalculator {

    private static final BigDecimal P30 = new BigDecimal("0.30");
    private static final BigDecimal P01 = new BigDecimal("0.01");
    private static final BigDecimal CEM = new BigDecimal("100");

    public BigDecimal calculate(BigDecimal vlrBruto, List<Discount> descontos, int dtHoje) {
        BigDecimal total = contribSocial(vlrBruto);
        BigDecimal max = MoneyMath.trunc2(vlrBruto.multiply(P30));

        for (Discount d : descontos) {
            // Vigencia (CALCDSCT.NSN#L115-L122)
            if (d.dtFim() != 0 && d.dtFim() < dtHoje) {
                continue;
            }
            if (d.dtInicio() > dtHoje) {
                continue;
            }

            BigDecimal item;
            switch (d.tipo()) {
                case 'J', 'P', 'A' -> item = (d.vlr().compareTo(BigDecimal.ZERO) > 0)
                        ? d.vlr()
                        : vlrBruto.multiply(d.pct().divide(CEM));
                case 'I' -> item = vlrBruto.multiply(d.pct().divide(CEM));
                case 'S' -> item = vlrBruto.multiply(P01);
                default -> { // NONE -> IGNORE
                    continue;
                }
            }
            total = total.add(item);

            // Teto 30% — exceto judicial (CALCDSCT.NSN#L164-L169)
            if (d.tipo() != 'J' && total.compareTo(max) > 0) {
                total = max;
            }
        }

        return MoneyMath.trunc2(total);
    }

    /** Contribuicao social progressiva (CALC-CONTRIB-SOCIAL, BR-003). */
    BigDecimal contribSocial(BigDecimal vlrBruto) {
        BigDecimal[][] faixas = {
            {new BigDecimal("500.00"), new BigDecimal("0.03")},
            {new BigDecimal("1000.00"), new BigDecimal("0.05")},
            {new BigDecimal("2000.00"), new BigDecimal("0.07")},
            {new BigDecimal("9999.99"), new BigDecimal("0.09")}
        };
        for (BigDecimal[] faixa : faixas) {
            if (vlrBruto.compareTo(faixa[0]) <= 0) {
                return vlrBruto.multiply(faixa[1]);
            }
        }
        return BigDecimal.ZERO;
    }
}
