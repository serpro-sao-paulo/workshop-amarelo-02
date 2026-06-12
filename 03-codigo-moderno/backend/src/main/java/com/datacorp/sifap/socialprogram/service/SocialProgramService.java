package com.datacorp.sifap.socialprogram.service;

import com.datacorp.sifap.socialprogram.domain.EligibilityValidator;
import com.datacorp.sifap.socialprogram.domain.EligibilityValidator.Result;
import com.datacorp.sifap.socialprogram.domain.SocialProgram;
import com.datacorp.sifap.socialprogram.repository.SocialProgramRepository;
import com.datacorp.sifap.beneficiary.repository.BeneficiaryRepository;
import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service do contexto Social Program.
 * REQ-007 (manutencao de programas), REQ-008 (elegibilidade).
 */
@Service
public class SocialProgramService {

    private final SocialProgramRepository repo;
    private final BeneficiaryRepository beneficiaryRepo;
    private final EligibilityValidator eligibilityValidator;

    public SocialProgramService(SocialProgramRepository repo,
                                BeneficiaryRepository beneficiaryRepo,
                                EligibilityValidator eligibilityValidator) {
        this.repo = repo;
        this.beneficiaryRepo = beneficiaryRepo;
        this.eligibilityValidator = eligibilityValidator;
    }

    /** REQ-007: cadastra programa social. source_legacy: CADPROG.NSN. */
    @Transactional
    public SocialProgram create(SocialProgram program) {
        if (repo.existsByProgramCode(program.getProgramCode())) {
            throw new IllegalStateException("Programa ja cadastrado");
        }
        // Defaults de controle (colunas NOT NULL na V002): status='A', created_at
        if (program.getStatus() == null || program.getStatus().isBlank()) {
            program.setStatus("A");
        }
        program.setCreatedAt(LocalDate.now());
        // Religa a FK social_program_id (NOT NULL) das coleções aninhadas
        program.getCalculationTiers().forEach(tier -> tier.setProgram(program));
        program.getRegionalParams().forEach(param -> param.setProgram(program));
        return repo.save(program);
    }

    @Transactional
    public Optional<SocialProgram> findByCode(String code) {
        Optional<SocialProgram> program = repo.findByProgramCode(code);
        // Inicializa coleções lazy dentro da sessão (open-in-view=false)
        program.ifPresent(p -> {
            p.getCalculationTiers().size();
            p.getRegionalParams().size();
            p.getApplicableDiscountTypes().size();
        });
        return program;
    }

    /**
     * REQ-008: avalia elegibilidade. source_legacy: VALELEG.NSN.
     * Delega a logica de dominio para {@link EligibilityValidator}.
     */
    public Result checkEligibility(String cpf, String programCode) {
        Beneficiary b = beneficiaryRepo.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiario nao encontrado"));
        SocialProgram p = repo.findByProgramCode(programCode)
                .orElseThrow(() -> new IllegalArgumentException("Programa nao encontrado"));
        if (!"A".equals(p.getStatus())) {
            throw new IllegalStateException("Programa inativo");
        }
        int anoAtual = java.time.LocalDate.now().getYear();
        int anoNasc = b.getBirthDate().getYear();
        int idade = anoAtual - anoNasc;
        char codReg = b.getRegionCode() != null
                ? b.getRegionCode().charAt(0) : '0';
        int codRegiao = b.getRegionCode() != null
                ? Integer.parseInt(b.getRegionCode().trim()) : 0;
        BigDecimal rendaMax = p.getMaxPerCapitaIncome() != null
                ? p.getMaxPerCapitaIncome() : BigDecimal.ZERO;
        long nis = 0L; // NIS nao mapeado nesta view simplificada
        return eligibilityValidator.validate(
                codRegiao,
                b.getStatus().charAt(0),
                idade,
                b.getFamilyIncome() != null ? b.getFamilyIncome() : BigDecimal.ZERO,
                b.getDependents().size(),
                nis,
                p.getProgramType().charAt(0),
                p.getMinAge() != null ? p.getMinAge() : 0,
                p.getMaxAge() != null ? p.getMaxAge() : 0,
                rendaMax,
                null, // codEleg simplificado
                'S')  // docsOk simplificado
        ;
    }
}
