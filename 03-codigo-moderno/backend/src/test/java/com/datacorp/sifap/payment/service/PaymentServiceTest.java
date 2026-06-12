package com.datacorp.sifap.payment.service;

import com.datacorp.sifap.SifapTestBase;
import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import com.datacorp.sifap.beneficiary.repository.BeneficiaryRepository;
import com.datacorp.sifap.payment.domain.Payment;
import com.datacorp.sifap.socialprogram.domain.SocialProgram;
import com.datacorp.sifap.socialprogram.repository.SocialProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integracao para PaymentService.
 * REQ-009 (so ativos), REQ-010 (idempotencia), REQ-013 (motor de calculo), REQ-021 (status G).
 */
@Transactional
class PaymentServiceTest extends SifapTestBase {

    @Autowired PaymentService service;
    @Autowired BeneficiaryRepository beneficiaryRepo;
    @Autowired SocialProgramRepository programRepo;

    private static final String CPF = "11144477735";
    private static final String COMPETENCE = "202506";

    @BeforeEach
    void setup() {
        SocialProgram prog = new SocialProgram();
        prog.setProgramCode("PBF1");
        prog.setName("Programa Bolsa Familia");
        prog.setProgramType("A");
        prog.setStatus("A");
        prog.setBaseValueIndividual(new BigDecimal("1000.00"));
        prog.setAnnualAdjustmentPct(BigDecimal.ZERO);
        programRepo.save(prog);

        Beneficiary b = new Beneficiary();
        b.setCpf(CPF);
        b.setFullName("JOSE DA SILVA");
        b.setMotherName("MARIA");
        b.setBirthDate(LocalDate.of(1980, 1, 15));
        b.setStatus("A");
        b.setProgramCode("PBF1");
        b.setRegionCode("11"); // Sudeste, fator 1.10
        b.setFamilyIncome(new BigDecimal("300.00"));
        beneficiaryRepo.save(b);
    }

    @Test
    void should_generate_payment_with_status_G_when_beneficiary_active() {
        // REQ-021: status inicial G
        Payment p = service.generateForBeneficiary(CPF, COMPETENCE);
        assertThat(p.getId()).isNotNull();
        assertThat(p.getStatus()).isEqualTo("G");
        assertThat(p.getGrossAmount()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void should_reject_inactive_beneficiary() {
        // REQ-009
        Beneficiary b = beneficiaryRepo.findByCpf(CPF).orElseThrow();
        b.setStatus("S");
        beneficiaryRepo.save(b);
        assertThatThrownBy(() -> service.generateForBeneficiary(CPF, COMPETENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ativo");
    }

    @Test
    void should_reject_duplicate_payment_same_competence() {
        // REQ-010: idempotencia
        service.generateForBeneficiary(CPF, COMPETENCE);
        assertThatThrownBy(() -> service.generateForBeneficiary(CPF, COMPETENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("competencia");
    }

    @Test
    void should_calculate_december_payment_with_type_D() {
        // REQ-014: dezembro -> tipo D + 13o
        Payment p = service.generateForBeneficiary(CPF, "202512");
        assertThat(p.getPaymentType()).isEqualTo("D");
        assertThat(p.getGrossAmount()).isGreaterThan(new BigDecimal("1000.00"));
    }
}
