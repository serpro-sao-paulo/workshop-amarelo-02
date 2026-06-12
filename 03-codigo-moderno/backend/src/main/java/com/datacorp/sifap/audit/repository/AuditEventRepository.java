package com.datacorp.sifap.audit.repository;

import com.datacorp.sifap.audit.domain.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    /**
     * Consulta trilha completa (REQ-019: inclui EX, sem filtro de acao).
     * Ordenada por data+hora desc para exibir eventos mais recentes primeiro.
     */
    @Query("""
        SELECT a FROM AuditEvent a
        WHERE (:from IS NULL OR a.eventDate >= :from)
          AND (:to   IS NULL OR a.eventDate <= :to)
          AND (:cpf  IS NULL OR a.affectedCpf = :cpf)
        ORDER BY a.eventDate DESC, a.eventTime DESC
        """)
    Page<AuditEvent> findByFilters(
            @Param("from") LocalDate from,
            @Param("to")   LocalDate to,
            @Param("cpf")  String cpf,
            Pageable pageable);
}
