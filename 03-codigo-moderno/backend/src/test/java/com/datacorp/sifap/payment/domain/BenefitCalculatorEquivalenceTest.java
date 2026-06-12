package com.datacorp.sifap.payment.domain;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Testes de equivalencia para {@link BenefitCalculator} vs. CALCBENF.
 * source_legacy: CALCBENF.NSN#L186-L296 (BR-004, BR-006 / REQ-013, REQ-014).
 *
 * <p>Branches cobertos: fator regional (dentro/fora de 1..25), familiar
 * (0/&le;2/&le;4/&gt;4), renda (5 faixas), idade (&ge;65/&ge;60/&lt;18/demais),
 * dezembro (tipo D + 13o), abono tipo 'A', desconto inline 3% (bruto &gt; 500),
 * competencia invalida.
 */
class BenefitCalculatorEquivalenceTest {

    private final BenefitCalculator calc = new BenefitCalculator();

    @ParameterizedTest
    @CsvSource({
        // base,    reaj,   reg, dep, renda,   dtNasc,   comp,   prog, expBruto, expDesc, expLiq,  expTipo
        "1000.00, 0.0000, 11, 0, 300.00, 19800101, 202506, P, 1100.00, 33.00, 1067.00, N", // normal reg=1.10
        "1000.00, 0.0000, 11, 0, 800.00, 19800101, 202506, P,  770.00, 23.10,  746.90, N", // faixa renda 0.70
        "1000.00, 0.0000, 11, 3, 300.00, 19800101, 202506, P, 1243.00, 37.29, 1205.71, N", // familiar 3 dep = 1.13
        "1000.00, 0.0000, 11, 0, 300.00, 19550101, 202506, P, 1265.00, 37.95, 1227.05, N", // idade 70 = 1.15
        "1000.00, 0.0000, 99, 0, 300.00, 19800101, 202506, P, 1000.00, 30.00,  970.00, N", // regiao fora de 1..25 = 1.0
        "1000.00, 0.1000, 15, 0, 300.00, 19800101, 202506, P, 1100.00, 33.00, 1067.00, N", // reajuste 10%, reg 1.0
        "100.00,  0.0000, 15, 0, 300.00, 19800101, 202506, P,  100.00,  0.00,  100.00, N", // bruto <= 500 sem desconto
        "1000.00, 0.0000, 11, 0, 300.00, 19800101, 202512, A, 2365.00, 70.95, 2294.05, D", // dezembro + 13o + abono 15%
        "1000.00, 0.0000, 11, 0, 300.00, 19800101, 202512, P, 2200.00, 66.00, 2134.00, D"  // dezembro + 13o, sem abono (tipo != A)
    })
    void should_match_natural_benefit(BigDecimal base, BigDecimal reaj, int reg, int dep,
                                      BigDecimal renda, int dtNasc, int comp, char prog,
                                      BigDecimal expBruto, BigDecimal expDesc,
                                      BigDecimal expLiq, char expTipo) {
        BenefitResult r = calc.calculate(base, reaj, reg, dep, renda, dtNasc, comp, prog);
        assertThat(r.vlrBruto()).isEqualByComparingTo(expBruto);
        assertThat(r.vlrDesconto()).isEqualByComparingTo(expDesc);
        assertThat(r.vlrLiquido()).isEqualByComparingTo(expLiq);
        assertThat(r.tipoPgto()).isEqualTo(expTipo);
    }

    @Test
    void should_reject_invalid_competence() {
        // CALCBENF.NSN#L? IF #MES < 1 OR #MES > 12 -> COMPETENCIA INVALIDA
        assertThatThrownBy(() ->
                calc.calculate(new BigDecimal("1000.00"), new BigDecimal("0.0000"),
                        11, 0, new BigDecimal("300.00"), 19800101, 202513, 'P'))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @Disabled("MYSTERY MYS-006: o desconto aqui e 3% inline (CALCBENF.NSN#L315-L322), "
            + "divergente do motor CALCDSCT (teto 30% + tipos). Equivalencia entre os "
            + "dois caminhos nao e definivel ate o PO escolher a regra canonica.")
    void mystery_inline_discount_vs_calcdsct() {
        fail("Precisa de decisao da equipe - veja MYS-006");
    }
}
