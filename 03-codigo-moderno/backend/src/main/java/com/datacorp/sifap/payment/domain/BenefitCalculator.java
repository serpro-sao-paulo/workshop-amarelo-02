package com.datacorp.sifap.payment.domain;

import com.datacorp.sifap.shared.MoneyMath;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Motor de calculo do beneficio mensal, traducao de CALCBENF.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L186-L296}.
 * <br>Regras: BR-004 (calculo), BR-006 (13o/abono). REQ-013, REQ-014.
 */
@Component
public class BenefitCalculator {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal P15 = new BigDecimal("0.15");
    private static final BigDecimal P03 = new BigDecimal("0.03");
    private static final BigDecimal D500 = new BigDecimal("500.00");

    /**
     * @param tipoPrograma 'A' habilita o abono natalino de 15% em dezembro.
     */
    public BenefitResult calculate(BigDecimal vlrBase,
                                   BigDecimal fatorReajuste,
                                   int codRegiao,
                                   int numDependentes,
                                   BigDecimal rendaFamiliar,
                                   int dtNascimento,
                                   int competencia,
                                   char tipoPrograma) {
        int ano = competencia / 100;
        int mes = competencia - (ano * 100);
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("COMPETENCIA INVALIDA");
        }
        int idade = ano - (dtNascimento / 10000);

        BigDecimal fatorReg = RegionalFactorTable.factor(codRegiao);
        BigDecimal fatorFam = familyFactor(numDependentes);
        BigDecimal fatorRnd = incomeFactor(rendaFamiliar);
        BigDecimal fatorIdade = ageFactor(idade);

        // VLR = BASE * REG * FAM * RND * IDADE (campo N9.2 -> trunca)
        BigDecimal benf = MoneyMath.trunc2(
                vlrBase.multiply(fatorReg).multiply(fatorFam)
                        .multiply(fatorRnd).multiply(fatorIdade));
        // Reajuste do programa (campo N9.2 -> trunca)
        benf = MoneyMath.trunc2(benf.multiply(ONE.add(fatorReajuste)));

        BigDecimal bruto = benf;
        BigDecimal vlr13 = BigDecimal.ZERO.setScale(2);
        BigDecimal abono = BigDecimal.ZERO.setScale(2);
        char tipoPgto = 'N';

        if (mes == 12) {
            tipoPgto = 'D';
            vlr13 = MoneyMath.trunc2(vlrBase.multiply(fatorReg).multiply(fatorIdade));
            bruto = benf.add(vlr13);
            if (tipoPrograma == 'A') {
                abono = MoneyMath.trunc2(benf.multiply(P15));
                bruto = bruto.add(abono);
            }
        }

        // CALC-DESCONTOS inline (CALCBENF.NSN#L315-L322): 3% se bruto > 500
        BigDecimal desconto = BigDecimal.ZERO.setScale(2);
        if (bruto.compareTo(D500) > 0) {
            desconto = MoneyMath.trunc2(bruto.multiply(P03));
        }

        BigDecimal liquido = bruto.subtract(desconto);
        if (liquido.compareTo(BigDecimal.ZERO) < 0) {
            liquido = BigDecimal.ZERO;
        }
        liquido = MoneyMath.trunc2(liquido);

        return new BenefitResult(MoneyMath.trunc2(bruto), desconto, liquido,
                vlr13, abono, tipoPgto);
    }

    /** Fator familiar (CALCBENF.NSN#L223-L237). */
    BigDecimal familyFactor(int dep) {
        if (dep == 0) {
            return new BigDecimal("1.0000");
        }
        if (dep <= 2) {
            return new BigDecimal("1.0000").add(new BigDecimal(dep).multiply(new BigDecimal("0.0500")));
        }
        if (dep <= 4) {
            return new BigDecimal("1.1000").add(new BigDecimal(dep - 2).multiply(new BigDecimal("0.0300")));
        }
        return new BigDecimal("1.1600").add(new BigDecimal(dep - 4).multiply(new BigDecimal("0.0200")));
    }

    /** Fator de renda — primeira faixa cujo teto >= renda (DET-FAIXA-RENDA). */
    BigDecimal incomeFactor(BigDecimal renda) {
        BigDecimal[][] faixas = {
            {new BigDecimal("300.00"), new BigDecimal("1.0000")},
            {new BigDecimal("600.00"), new BigDecimal("0.8500")},
            {new BigDecimal("1000.00"), new BigDecimal("0.7000")},
            {new BigDecimal("1500.00"), new BigDecimal("0.5500")},
            {new BigDecimal("9999.99"), new BigDecimal("0.4000")}
        };
        for (BigDecimal[] faixa : faixas) {
            if (renda.compareTo(faixa[0]) <= 0) {
                return faixa[1];
            }
        }
        return new BigDecimal("0.4000");
    }

    /** Fator idade (CALCBENF.NSN#L262-L276). */
    BigDecimal ageFactor(int idade) {
        if (idade >= 65) {
            return new BigDecimal("1.1500");
        }
        if (idade >= 60) {
            return new BigDecimal("1.1000");
        }
        if (idade < 18) {
            return new BigDecimal("1.0500");
        }
        return new BigDecimal("1.0000");
    }
}
