package br.gov.sifap.payment.domain;

import br.gov.sifap.payment.spi.BeneficiarySnapshot;
import br.gov.sifap.payment.spi.ProgramParameters;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import org.springframework.stereotype.Component;

/**
 * Implementação fiel ao motor legado {@code CALCBENF.NSN} (BR-004).
 *
 * <p>Fórmula: {@code base × fatorRegional × fatorFamiliar × fatorRenda × fatorIdade},
 * depois {@code × (1 + reajuste)}, truncado em 2 casas (mainframe trunca, não
 * arredonda — {@code RoundingMode.DOWN}, decisão D4/research).
 *
 * <p>As tabelas e faixas reproduzem exatamente {@code CALCBENF.NSN} (linhas
 * L91-L131, L179-L219, L303-L311).
 */
@Component
public class DefaultBenefitCalculator implements BenefitCalculator {

    /** Escala monetária. */
    private static final int MONEY_SCALE = 2;
    private static final int FACTOR_SCALE = 4;

    /**
     * Fator regional por código de região 1–25 (CALCBENF.NSN#L91-L115).
     * Índice 0 não é usado; regiões fora de 1–25 usam 1,0000 (L181-L184).
     */
    private static final BigDecimal[] REGIONAL_FACTOR = {
            null,                       // 0 — não usado
            bd("1.3500"), // 1  AC
            bd("1.3200"), // 2  AM
            bd("1.3000"), // 3  AP
            bd("1.2800"), // 4  PA
            bd("1.3100"), // 5  RO
            bd("1.4000"), // 6  MA
            bd("1.3800"), // 7  PI
            bd("1.3500"), // 8  CE
            bd("1.3200"), // 9  BA
            bd("1.3600"), // 10 PE
            bd("1.1000"), // 11 SP
            bd("1.1200"), // 12 RJ
            bd("1.0800"), // 13 MG
            bd("1.0500"), // 14 ES
            bd("1.0000"), // 15 REF
            bd("1.0500"), // 16 PR
            bd("1.0700"), // 17 SC
            bd("1.0300"), // 18 RS
            bd("1.1500"), // 19 MS
            bd("1.2000"), // 20 MT
            bd("1.1800"), // 21 GO
            bd("1.2500"), // 22 TO
            bd("1.1000"), // 23 DF
            bd("1.2200"), // 24 RR
            bd("1.3300")  // 25 SE
    };

    /** Tetos das faixas de renda (CALCBENF.NSN#L120-L131). */
    private static final BigDecimal[] INCOME_CEILING = {
            bd("300.00"), bd("600.00"), bd("1000.00"), bd("1500.00"), bd("9999.99")
    };

    /** Fatores das faixas de renda, na mesma ordem dos tetos. */
    private static final BigDecimal[] INCOME_FACTOR = {
            bd("1.0000"), bd("0.8500"), bd("0.7000"), bd("0.5500"), bd("0.4000")
    };

    private static final BigDecimal ONE = bd("1.0000");

    @Override
    public BigDecimal calculateGross(BeneficiarySnapshot beneficiary,
                                     ProgramParameters program,
                                     YearMonth competence) {
        BigDecimal regional = regionalFactor(beneficiary.regionCode());
        BigDecimal family = familyFactor(beneficiary.dependentsCount());
        BigDecimal income = incomeFactor(beneficiary.familyIncome());
        BigDecimal age = ageFactor(ageAt(beneficiary, competence));

        BigDecimal value = program.baseValue()
                .multiply(regional)
                .multiply(family)
                .multiply(income)
                .multiply(age);

        // Reajuste do programa: × (1 + reajuste)
        value = value.multiply(ONE.add(program.adjustmentFactor()));

        // Truncamento mainframe: 2 casas, sem arredondar (BR-004).
        return value.setScale(MONEY_SCALE, RoundingMode.DOWN);
    }

    /** CALCBENF.NSN#L179-L184. */
    private BigDecimal regionalFactor(int regionCode) {
        if (regionCode >= 1 && regionCode <= 25) {
            return REGIONAL_FACTOR[regionCode];
        }
        return ONE;
    }

    /** CALCBENF.NSN#L186-L199. */
    private BigDecimal familyFactor(int dependents) {
        if (dependents <= 0) {
            return ONE;
        }
        if (dependents <= 2) {
            return ONE.add(bd("0.0500").multiply(BigDecimal.valueOf(dependents)));
        }
        if (dependents <= 4) {
            return bd("1.1000").add(bd("0.0300").multiply(BigDecimal.valueOf(dependents - 2L)));
        }
        return bd("1.1600").add(bd("0.0200").multiply(BigDecimal.valueOf(dependents - 4L)));
    }

    /** CALCBENF.NSN#L303-L311: primeira faixa cujo teto ≥ renda. */
    private BigDecimal incomeFactor(BigDecimal income) {
        for (int i = 0; i < INCOME_CEILING.length; i++) {
            if (income.compareTo(INCOME_CEILING[i]) <= 0) {
                return INCOME_FACTOR[i];
            }
        }
        // Renda acima do maior teto: legado não casa nenhuma faixa.
        // Aplica-se o menor fator (faixa mais alta) de forma conservadora.
        return INCOME_FACTOR[INCOME_FACTOR.length - 1];
    }

    /** CALCBENF.NSN#L204-L219. */
    private BigDecimal ageFactor(int age) {
        if (age >= 65) {
            return bd("1.1500");
        }
        if (age >= 60) {
            return bd("1.1000");
        }
        if (age < 18) {
            return bd("1.0500");
        }
        return ONE;
    }

    /** CALCBENF.NSN#L221-L222: idade apenas pelo ano (#ANO - #ANO-NASC). */
    private int ageAt(BeneficiarySnapshot beneficiary, YearMonth competence) {
        return competence.getYear() - beneficiary.birthDate().getYear();
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value).setScale(FACTOR_SCALE, RoundingMode.UNNECESSARY);
    }
}
