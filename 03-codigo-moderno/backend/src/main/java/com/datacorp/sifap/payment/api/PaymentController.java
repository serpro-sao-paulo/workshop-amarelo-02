package com.datacorp.sifap.payment.api;

import com.datacorp.sifap.payment.domain.Payment;
import com.datacorp.sifap.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST do contexto Payment &amp; Cycle.
 * REQ-009..REQ-023. Path: /api/v1/payments, /api/v1/payment-cycles.
 */
@RestController
@Tag(name = "Payment & Cycle",
     description = "Ciclo de pagamento, cálculo e consulta")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @Operation(summary = "Gera pagamento para um beneficiário na competência",
               description = "REQ-009, REQ-010, REQ-013..REQ-015, REQ-021. " +
                             "Status inicial 'G'. Idempotente por CPF+competência.")
    @ApiResponse(responseCode = "201", description = "Pagamento gerado")
    @ApiResponse(responseCode = "409", description = "Pagamento já existe na competência")
    @PostMapping("/api/v1/payment-cycles")
    public ResponseEntity<Payment> generatePayment(
            @RequestParam String cpf,
            @RequestParam String competence) {
        return ResponseEntity.status(201).body(service.generateForBeneficiary(cpf, competence));
    }

    @Operation(summary = "Consulta pagamentos de um beneficiário",
               description = "REQ-011, REQ-012. Ordenado por competência desc.")
    @ApiResponse(responseCode = "200", description = "Lista de pagamentos")
    @GetMapping("/api/v1/payments")
    public ResponseEntity<List<Payment>> findPayments(
            @RequestParam String cpf,
            @RequestParam(required = false) String competence) {
        if (competence != null) {
            return ResponseEntity.ok(
                    service.findByCpfAndCompetence(cpf, competence).map(List::of).orElse(List.of()));
        }
        return ResponseEntity.ok(service.findByCpf(cpf));
    }
}
