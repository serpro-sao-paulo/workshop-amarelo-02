package com.datacorp.sifap.socialprogram.api;

import com.datacorp.sifap.socialprogram.domain.EligibilityValidator.Result;
import com.datacorp.sifap.socialprogram.domain.SocialProgram;
import com.datacorp.sifap.socialprogram.service.SocialProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Controller REST do contexto Social Program.
 * REQ-007, REQ-008. Path: /api/v1/social-programs.
 */
@RestController
@RequestMapping("/api/v1/social-programs")
@Tag(name = "Social Program",
     description = "Parâmetros de programas sociais e elegibilidade")
public class SocialProgramController {

    private final SocialProgramService service;

    public SocialProgramController(SocialProgramService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra um programa social", description = "REQ-007.")
    @ApiResponse(responseCode = "201", description = "Programa criado")
    @ApiResponse(responseCode = "409", description = "Código já cadastrado")
    @PostMapping
    public ResponseEntity<SocialProgram> create(@RequestBody SocialProgram body) {
        SocialProgram saved = service.create(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{code}").buildAndExpand(saved.getProgramCode()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @Operation(summary = "Consulta parâmetros de um programa social", description = "REQ-007.")
    @ApiResponse(responseCode = "200", description = "Programa encontrado")
    @ApiResponse(responseCode = "404", description = "Programa não encontrado")
    @GetMapping("/{code}")
    public ResponseEntity<SocialProgram> findByCode(@PathVariable String code) {
        return service.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Avalia elegibilidade de um beneficiário no programa",
               description = "REQ-008. Tipo A/P/T; região 99 concede bypass (MYS-010).")
    @ApiResponse(responseCode = "200", description = "Resultado de elegibilidade")
    @GetMapping("/{code}/eligibility")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @PathVariable String code,
            @RequestParam String cpf) {
        Result r = service.checkEligibility(cpf, code);
        return ResponseEntity.ok(new EligibilityResponse(r.eligible(), r.reasons()));
    }

    public record EligibilityResponse(boolean eligible, java.util.List<String> reasons) {}
}
