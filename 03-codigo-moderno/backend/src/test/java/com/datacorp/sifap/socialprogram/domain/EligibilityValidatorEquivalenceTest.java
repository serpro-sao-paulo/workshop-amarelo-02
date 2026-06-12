package com.datacorp.sifap.socialprogram.domain;

import com.datacorp.sifap.socialprogram.domain.EligibilityValidator.Result;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de equivalencia para {@link EligibilityValidator} vs. VALELEG.
 * source_legacy: VALELEG.NSN#L104-L242 (BR-013, BR-014 / MYS-010, MYS-026, MYS-027).
 */
class EligibilityValidatorEquivalenceTest {

    private final EligibilityValidator v = new EligibilityValidator();
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Test
    void region_99_grants_eligibility_bypassing_everything() {
        // MYS-010: regiao 99 -> elegivel mesmo com status cancelado e idade invalida
        Result r = v.validate(99, 'C', 5, new BigDecimal("9999.00"), 0, 0, 'T',
                0, 0, ZERO, null, 'N');
        assertThat(r.eligible()).isTrue();
        assertThat(r.reasons()).isEmpty();
    }

    @Test
    void type_P_requires_min_age_60() {
        Result reprovado = v.validate(11, 'A', 55, new BigDecimal("100.00"), 0, 0, 'P',
                0, 0, ZERO, null, 'S');
        assertThat(reprovado.eligible()).isFalse();
        assertThat(reprovado.reasons()).contains("PROG PREVIDENCIARIO: IDADE < 60");

        Result aprovado = v.validate(11, 'A', 62, new BigDecimal("100.00"), 0, 0, 'P',
                0, 0, ZERO, null, 'S');
        assertThat(aprovado.eligible()).isTrue();
    }

    @Test
    void type_T_requires_age_between_16_and_65() {
        assertThat(v.validate(11, 'A', 70, new BigDecimal("100.00"), 0, 0, 'T',
                0, 0, ZERO, null, 'S').eligible()).isFalse();
        assertThat(v.validate(11, 'A', 30, new BigDecimal("100.00"), 0, 0, 'T',
                0, 0, ZERO, null, 'S').eligible()).isTrue();
    }

    @Test
    void type_A_income_quirk_and_docs() {
        // renda > 600 SEM dependentes -> reprova
        assertThat(v.validate(11, 'A', 40, new BigDecimal("700.00"), 0, 0, 'A',
                0, 0, ZERO, null, 'S').eligible()).isFalse();
        // QUIRK: renda > 600 COM dependentes -> nao reprova por renda; docs OK -> elegivel
        assertThat(v.validate(11, 'A', 40, new BigDecimal("700.00"), 2, 0, 'A',
                0, 0, ZERO, null, 'S').eligible()).isTrue();
        // docs != 'S' -> reprova por documentacao
        Result semDocs = v.validate(11, 'A', 40, new BigDecimal("300.00"), 0, 0, 'A',
                0, 0, ZERO, null, 'N');
        assertThat(semDocs.eligible()).isFalse();
        assertThat(semDocs.reasons()).contains("DOCUMENTACAO INCOMPLETA");
    }

    @Test
    void cod_elegibilidade_requires_nis_and_dependents() {
        // MYS-026: codEleg "RD" -> char1 R exige NIS, char2 D exige dependentes
        Result r = v.validate(11, 'A', 40, new BigDecimal("100.00"), 0, 0, 'P',
                0, 0, ZERO, "RD", 'S');
        // idade 40 < 60 ja reprova tipo P; alem disso NIS=0 e numDep=0
        assertThat(r.eligible()).isFalse();
        assertThat(r.reasons()).contains("NIS NAO CADASTRADO");
        assertThat(r.reasons()).contains("PROGRAMA REQUER DEPENDENTES");
    }

    @Test
    void suspended_beneficiary_is_not_eligible() {
        Result r = v.validate(11, 'S', 62, new BigDecimal("100.00"), 0, 0, 'P',
                0, 0, ZERO, null, 'S');
        assertThat(r.eligible()).isFalse();
        assertThat(r.reasons()).contains("BENEFICIARIO SUSPENSO");
    }
}
