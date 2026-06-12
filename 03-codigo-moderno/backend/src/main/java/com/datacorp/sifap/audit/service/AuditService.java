package com.datacorp.sifap.audit.service;

import com.datacorp.sifap.audit.domain.AuditEvent;
import com.datacorp.sifap.audit.repository.AuditEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service do contexto Audit Trail.
 * REQ-018 (registro), REQ-019 (trilha completa inclui EX), REQ-020.
 */
@Service
public class AuditService {

    private final AuditEventRepository repo;

    public AuditService(AuditEventRepository repo) {
        this.repo = repo;
    }

    /** REQ-018, REQ-020: registra um evento de auditoria. Registro imutavel. */
    @Transactional
    public AuditEvent record(AuditEvent event) {
        if (event.getEventDate() == null) {
            event.setEventDate(LocalDate.now());
        }
        if (event.getEventTime() == null) {
            event.setEventTime(LocalTime.now());
        }
        return repo.save(event);
    }

    /**
     * REQ-019: consulta trilha completa SEM filtrar 'EX' (corrige BR-011 do legado).
     * Filtros opcionais: from, to, cpf.
     */
    public Page<AuditEvent> query(LocalDate from, LocalDate to, String cpf, Pageable pageable) {
        return repo.findByFilters(from, to, cpf, pageable);
    }
}
