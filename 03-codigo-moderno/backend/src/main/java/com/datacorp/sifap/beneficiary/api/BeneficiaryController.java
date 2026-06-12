package com.datacorp.sifap.beneficiary.api;

import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import com.datacorp.sifap.beneficiary.domain.CpfMask;
import com.datacorp.sifap.beneficiary.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller REST do contexto Beneficiary Management.
 * REQ-001..REQ-006. Convenção de path: /api/v1/beneficiaries.
 */
@RestController
@RequestMapping("/api/v1/beneficiaries")
@Tag(name = "Beneficiary Management",
     description = "Cadastro, validação e consulta de beneficiários")
public class BeneficiaryController {

    private final BeneficiaryService service;
    private final CpfMask cpfMask;

    public BeneficiaryController(BeneficiaryService service, CpfMask cpfMask) {
        this.service = service;
        this.cpfMask = cpfMask;
    }

    @Operation(summary = "Cadastra um beneficiário",
               description = "REQ-001..REQ-004. CPF validado por módulo 11.")
    @ApiResponse(responseCode = "201", description = "Beneficiário criado")
    @ApiResponse(responseCode = "409", description = "CPF já cadastrado")
    @ApiResponse(responseCode = "422", description = "Falha de validação")
    @PostMapping
    public ResponseEntity<BeneficiarySummaryResponse> create(
            @Valid @RequestBody Beneficiary body) {
        Beneficiary saved = service.create(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{cpf}").buildAndExpand(saved.getCpf()).toUri();
        return ResponseEntity.created(location).body(toSummary(saved));
    }

    @Operation(summary = "Consulta um beneficiário com CPF mascarado",
               description = "REQ-006. CPF exibido mascarado (LGPD).")
    @ApiResponse(responseCode = "200", description = "Beneficiário encontrado")
    @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    @GetMapping("/{cpf}")
    public ResponseEntity<BeneficiarySummaryResponse> findByCpf(@PathVariable String cpf) {
        return service.findByCpf(cpf)
                .map(this::toSummary)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Altera um beneficiário",
               description = "REQ-004. Beneficiário deve existir.")
    @ApiResponse(responseCode = "200", description = "Beneficiário alterado")
    @ApiResponse(responseCode = "404", description = "Beneficiário não encontrado")
    @PutMapping("/{cpf}")
    public ResponseEntity<BeneficiarySummaryResponse> update(
            @PathVariable String cpf,
            @RequestBody Beneficiary patch) {
        return ResponseEntity.ok(toSummary(service.update(cpf, patch)));
    }

    /** REQ-006: aplica máscara de CPF na response (LGPD). */
    private BeneficiarySummaryResponse toSummary(Beneficiary b) {
        long cpfNum = Long.parseLong(b.getCpf());
        return new BeneficiarySummaryResponse(
                cpfMask.maskLegacy(cpfNum), // TODO: substituir por máscara LGPD conforme REQ-006
                b.getFullName(),
                b.getStatus());
    }

    public record BeneficiarySummaryResponse(String maskedCpf, String name, String status) {}
}
