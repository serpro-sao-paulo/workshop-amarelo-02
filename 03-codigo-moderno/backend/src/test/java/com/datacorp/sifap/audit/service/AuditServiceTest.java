package com.datacorp.sifap.audit.service;

import com.datacorp.sifap.SifapTestBase;
import com.datacorp.sifap.audit.domain.AuditEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes de integracao para AuditService.
 * REQ-018 (registro), REQ-019 (trilha completa, inclui EX), REQ-020.
 */
@Transactional
class AuditServiceTest extends SifapTestBase {

    @Autowired AuditService service;

    private AuditEvent event(String action, String cpf) {
        AuditEvent e = new AuditEvent();
        e.setAuditNumber(System.nanoTime());
        e.setEventDate(LocalDate.now());
        e.setEventTime(LocalTime.now());
        e.setEventTimestamp(System.currentTimeMillis());
        e.setActionCode(action);
        e.setEventUser("TEST");
        e.setAffectedCpf(cpf);
        e.setEntityType("BENF");
        return e;
    }

    @Test
    void should_record_audit_event() {
        // REQ-018
        AuditEvent saved = service.record(event("IN", "11144477735"));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getActionCode()).isEqualTo("IN");
    }

    @Test
    void should_include_exclusion_events_in_trail() {
        // REQ-019: corrige BR-011 do legado (RELAUDIT ocultava EX)
        service.record(event("IN", "11144477735"));
        service.record(event("EX", "11144477735"));
        Page<AuditEvent> result = service.query(
                null, null, "11144477735", PageRequest.of(0, 10));
        long exCount = result.stream()
                .filter(e -> "EX".equals(e.getActionCode())).count();
        assertThat(exCount).isEqualTo(1); // EX deve aparecer (REQ-019)
    }

    @Test
    void should_filter_by_date_and_cpf() {
        service.record(event("AL", "11144477735"));
        service.record(event("AL", "99999999999"));
        Page<AuditEvent> result = service.query(
                LocalDate.now(), LocalDate.now(), "11144477735", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAffectedCpf()).isEqualTo("11144477735");
    }
}
