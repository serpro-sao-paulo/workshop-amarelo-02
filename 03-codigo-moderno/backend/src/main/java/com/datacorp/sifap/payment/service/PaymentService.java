package com.datacorp.sifap.payment.service;

import com.datacorp.sifap.beneficiary.domain.Beneficiary;
import com.datacorp.sifap.beneficiary.repository.BeneficiaryRepository;
import com.datacorp.sifap.payment.domain.BenefitCalculator;
import com.datacorp.sifap.payment.domain.BenefitResult;
import com.datacorp.sifap.payment.domain.Payment;
import com.datacorp.sifap.payment.repository.PaymentRepository;
import com.datacorp.sifap.socialprogram.domain.SocialProgram;
import com.datacorp.sifap.socialprogram.repository.SocialProgramRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service do contexto Payment &amp; Cycle.
 * REQ-009..REQ-023.
 */
@Service
public class PaymentService {

    private final PaymentRepository repo;
    private final BeneficiaryRepository beneficiaryRepo;
    private final SocialProgramRepository programRepo;
    private final BenefitCalculator calculator;

    public PaymentService(PaymentRepository repo,
                          BeneficiaryRepository beneficiaryRepo,
                          SocialProgramRepository programRepo,
                          BenefitCalculator calculator) {
        this.repo = repo;
        this.beneficiaryRepo = beneficiaryRepo;
        this.programRepo = programRepo;
        this.calculator = calculator;
    }

    /** REQ-009, REQ-010, REQ-013..REQ-015, REQ-021: gera pagamento para um CPF na competencia. */
    @Transactional
    public Payment generateForBeneficiary(String cpf, String competence) {
        Beneficiary b = beneficiaryRepo.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiario nao encontrado"));

        // REQ-009: somente ativos
        if (!"A".equals(b.getStatus())) {
            throw new IllegalStateException("Beneficiario nao ativo: " + b.getStatus());
        }
        // REQ-010: idempotencia
        if (repo.existsByCpfAndCompetence(cpf, competence)) {
            throw new IllegalStateException("Pagamento ja existe para a competencia " + competence);
        }

        SocialProgram prog = programRepo.findByProgramCode(b.getProgramCode())
                .orElseThrow(() -> new IllegalArgumentException("Programa nao encontrado"));
        if (!"A".equals(prog.getStatus())) {
            throw new IllegalStateException("Programa inativo");
        }

        int dtNasc = b.getBirthDate().getYear() * 10000
                + b.getBirthDate().getMonthValue() * 100
                + b.getBirthDate().getDayOfMonth();
        int comp = Integer.parseInt(competence);

        // REQ-013: motor unico de calculo (BenefitCalculator)
        BenefitResult result = calculator.calculate(
                prog.getBaseValueIndividual() != null ? prog.getBaseValueIndividual() : java.math.BigDecimal.ZERO,
                prog.getAnnualAdjustmentPct() != null ? prog.getAnnualAdjustmentPct() : java.math.BigDecimal.ZERO,
                b.getRegionCode() != null ? Integer.parseInt(b.getRegionCode().trim()) : 0,
                b.getDependents().size(),
                b.getFamilyIncome() != null ? b.getFamilyIncome() : java.math.BigDecimal.ZERO,
                dtNasc, comp,
                prog.getProgramType().charAt(0));

        Payment payment = new Payment();
        payment.setCpf(cpf);
        payment.setProgramCode(b.getProgramCode());
        payment.setCompetence(competence);
        payment.setGrossAmount(result.vlrBruto());
        payment.setNetAmount(result.vlrLiquido());
        payment.setTotalDiscount(result.vlrDesconto());
        payment.setPaymentType(String.valueOf(result.tipoPgto()));
        payment.setStatus("G"); // REQ-021
        payment.setGenerationDate(LocalDate.now());
        return repo.save(payment);
    }

    @Transactional
    public List<Payment> findByCpf(String cpf) {
        List<Payment> payments = repo.findByCpfOrderByCompetenceDesc(cpf);
        // Inicializa coleção lazy dentro da sessão (open-in-view=false)
        payments.forEach(p -> p.getDiscounts().size());
        return payments;
    }

    @Transactional
    public Optional<Payment> findByCpfAndCompetence(String cpf, String competence) {
        Optional<Payment> payment = repo.findByCpfAndCompetence(cpf, competence);
        payment.ifPresent(p -> p.getDiscounts().size());
        return payment;
    }
}
