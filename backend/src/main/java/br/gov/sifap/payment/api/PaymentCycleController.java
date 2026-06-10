package br.gov.sifap.payment.api;

import br.gov.sifap.payment.application.CycleSummary;
import br.gov.sifap.payment.application.PaymentCycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint REST do ciclo de pagamento (REQ-PAY-001).
 */
@RestController
@RequestMapping("/api/v1/payment-cycles")
@Tag(name = "Payment Cycles", description = "Geração do ciclo de pagamento mensal")
public class PaymentCycleController {

    private final PaymentCycleService paymentCycleService;

    public PaymentCycleController(PaymentCycleService paymentCycleService) {
        this.paymentCycleService = paymentCycleService;
    }

    @Operation(summary = "Gera o ciclo de pagamento de uma competência")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ciclo gerado"),
            @ApiResponse(responseCode = "400", description = "Competência inválida"),
            @ApiResponse(responseCode = "409", description = "Ciclo já totalmente gerado")
    })
    @PostMapping
    public ResponseEntity<CycleSummary> generate(
            @Valid @RequestBody GeneratePaymentCycleRequest request) {
        CycleSummary summary = paymentCycleService.generate(request.competence());
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }
}
