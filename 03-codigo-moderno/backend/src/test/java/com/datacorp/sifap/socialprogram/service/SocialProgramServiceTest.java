package com.datacorp.sifap.socialprogram.service;

import com.datacorp.sifap.SifapTestBase;
import com.datacorp.sifap.socialprogram.domain.EligibilityValidator.Result;
import com.datacorp.sifap.socialprogram.domain.SocialProgram;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integracao para SocialProgramService.
 * REQ-007 (manutencao), REQ-008 (elegibilidade).
 */
@Transactional
class SocialProgramServiceTest extends SifapTestBase {

    @Autowired SocialProgramService service;

    private SocialProgram validProgram(String code) {
        SocialProgram p = new SocialProgram();
        p.setProgramCode(code);
        p.setName("Programa Teste");
        p.setProgramType("P");
        p.setStatus("A");
        p.setMinAge(60);
        return p;
    }

    @Test
    void should_create_program_successfully() {
        // REQ-007
        SocialProgram saved = service.create(validProgram("TST1"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProgramCode()).isEqualTo("TST1");
    }

    @Test
    void should_reject_duplicate_program_code() {
        // REQ-007
        service.create(validProgram("TST1"));
        assertThatThrownBy(() -> service.create(validProgram("TST1")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_find_program_by_code() {
        service.create(validProgram("TST2"));
        assertThat(service.findByCode("TST2")).isPresent();
        assertThat(service.findByCode("XXXX")).isEmpty();
    }
}
