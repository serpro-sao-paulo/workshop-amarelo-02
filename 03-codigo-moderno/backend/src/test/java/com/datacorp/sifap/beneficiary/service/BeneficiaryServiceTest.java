package com.datacorp.sifap.beneficiary.service;

import com.datacorp.sifap.SifapTestBase;
import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integracao para BeneficiaryService.
 * REQ-001..REQ-004 (validacao), REQ-016 (status inicial).
 */
@Transactional
class BeneficiaryServiceTest extends SifapTestBase {

    @Autowired
    BeneficiaryService service;

    private Beneficiary validBeneficiary(String cpf) {
        Beneficiary b = new Beneficiary();
        b.setCpf(cpf);
        b.setFullName("JOSE DA SILVA");
        b.setMotherName("MARIA DA SILVA");
        b.setBirthDate(LocalDate.of(1980, 1, 15));
        b.setGender("M");
        b.setFamilyIncome(new BigDecimal("500.00"));
        return b;
    }

    @Test
    void should_create_beneficiary_with_status_A_when_age_under_75() {
        // REQ-009 / BR-016: jovem -> status A
        Beneficiary saved = service.create(validBeneficiary("11144477735"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("A");
    }

    @Test
    void should_create_beneficiary_with_status_S_when_age_over_75() {
        // BR-016: acima de 75 -> status S (AJUSTE STATUS IDOSO 2011)
        Beneficiary b = validBeneficiary("11144477735");
        b.setBirthDate(LocalDate.of(1940, 1, 1)); // idade ~85
        Beneficiary saved = service.create(b);
        assertThat(saved.getStatus()).isEqualTo("S");
    }

    @Test
    void should_reject_invalid_cpf() {
        // REQ-001
        Beneficiary b = validBeneficiary("11111111111"); // todos iguais, sem excecao 000
        assertThatThrownBy(() -> service.create(b))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    void should_reject_duplicate_cpf() {
        // REQ-004
        service.create(validBeneficiary("11144477735"));
        assertThatThrownBy(() -> service.create(validBeneficiary("11144477735")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_reject_missing_name() {
        // REQ-003
        Beneficiary b = validBeneficiary("11144477735");
        b.setFullName("");
        assertThatThrownBy(() -> service.create(b))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome");
    }

    @Test
    void should_update_existing_beneficiary() {
        // REQ-004 (alteracao)
        service.create(validBeneficiary("11144477735"));
        Beneficiary patch = new Beneficiary();
        patch.setEmail("jose@example.com");
        Beneficiary updated = service.update("11144477735", patch);
        assertThat(updated.getEmail()).isEqualTo("jose@example.com");
    }

    @Test
    void should_throw_when_updating_nonexistent_beneficiary() {
        // REQ-004
        assertThatThrownBy(() -> service.update("99999999999", new Beneficiary()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
