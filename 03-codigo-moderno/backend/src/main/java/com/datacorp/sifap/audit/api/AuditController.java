package com.datacorp.sifap.audit.api;

import com.datacorp.sifap.audit.domain.AuditEvent;
import com.datacorp.sifap.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller REST do contexto Audit Trail.
 * REQ-018..REQ-020. Path: /api/v1/audit-events.
 */
@RestController
@RequestMapping("/api/v1/audit-events")
@Tag(name = "Audit Trail",
     description = "Consulta da trilha de auditoria (completa, inclui exclusões)")
public class AuditController {

    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @Operation(summary = "Consulta a trilha de auditoria",
               description = "REQ-018..REQ-020. Inclui eventos de exclusão (EX). " +
                             "Filtrável por data, CPF afetado. Paginado.")
    @ApiResponse(responseCode = "200", description = "Eventos de auditoria")
    @GetMapping
    public ResponseEntity<Page<AuditEvent>> query(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String cpf,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "eventDate", "eventTime"));
        return ResponseEntity.ok(service.query(from, to, cpf, pageable));
    }
}
