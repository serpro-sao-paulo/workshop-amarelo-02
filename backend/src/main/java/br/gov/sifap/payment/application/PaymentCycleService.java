package br.gov.sifap.payment.application;

import br.gov.sifap.payment.domain.BenefitBreakdown;
import br.gov.sifap.payment.domain.BenefitCalculator;
import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.repository.PaymentRepository;
import br.gov.sifap.payment.spi.BeneficiaryReader;
import br.gov.sifap.payment.spi.BeneficiarySnapshot;
import br.gov.sifap.payment.spi.ProgramParameters;
import br.gov.sifap.payment.spi.SocialProgramReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra a geração do ciclo de pagamento mensal (REQ-PAY-001/003/004/005/007/009).
 *
 * <p>Substitui o batch legado {@code BATCHPGT.NSN}, delegando o cálculo ao motor
 * único {@link BenefitCalculator} (sem reimplementar a fórmula inline — D2/research).
 */
@Service
public class PaymentCycleService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCycleService.class);
    private static final int MONEY_SCALE = 2;

    private final BeneficiaryReader beneficiaryReader;
    private final SocialProgramReader socialProgramReader;
    private final BenefitCalculator benefitCalculator;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    public PaymentCycleService(BeneficiaryReader beneficiaryReader,
                               SocialProgramReader socialProgramReader,
                               BenefitCalculator benefitCalculator,
                               PaymentRepository paymentRepository,
                               Clock clock) {
        this.beneficiaryReader = beneficiaryReader;
        this.socialProgramReader = socialProgramReader;
        this.benefitCalculator = benefitCalculator;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    /**
     * Gera os pagamentos da competência informada.
     *
     * @param competenceRaw competência no formato {@code AAAAMM}
     * @return resumo do ciclo
     * @throws InvalidCompetenceException     competência malformada ou mês fora de 1–12
     * @throws CycleAlreadyGeneratedException ciclo já totalmente gerado (idempotência)
     */
    @Transactional
    public CycleSummary generate(String competenceRaw) {
        YearMonth competence = parseCompetence(competenceRaw);
        String competenceKey = competenceRaw;
        LocalDate generationDate = LocalDate.now(clock);

        List<BeneficiarySnapshot> beneficiaries =
                beneficiaryReader.findActiveForCompetence(competenceKey);

        int generated = 0;
        int ignored = 0;
        int errors = 0;
        int preExisting = 0;
        BigDecimal totalGross = BigDecimal.ZERO.setScale(MONEY_SCALE);
        BigDecimal totalDiscount = BigDecimal.ZERO.setScale(MONEY_SCALE);
        BigDecimal totalNet = BigDecimal.ZERO.setScale(MONEY_SCALE);

        for (BeneficiarySnapshot beneficiary : beneficiaries) {
            try {
                // Idempotência: não duplica pagamento já existente (REQ-PAY-005).
                if (paymentRepository.existsByBeneficiaryCpfAndCompetence(
                        beneficiary.cpf(), competenceKey)) {
                    preExisting++;
                    ignored++;
                    continue;
                }

                // Programa inativo/inexistente → beneficiário ignorado (REQ-PAY-004).
                Optional<ProgramParameters> program =
                        socialProgramReader.findActiveByCode(beneficiary.programCode());
                if (program.isEmpty()) {
                    ignored++;
                    continue;
                }

                BenefitBreakdown breakdown = computeBreakdown(beneficiary, program.get(), competence);
                Payment payment = Payment.generateNormal(
                        beneficiary.cpf(), beneficiary.programCode(),
                        competenceKey, breakdown, generationDate);
                paymentRepository.save(payment);

                generated++;
                totalGross = totalGross.add(breakdown.grossAmount());
                totalDiscount = totalDiscount.add(breakdown.discountAmount());
                totalNet = totalNet.add(breakdown.netAmount());
            } catch (RuntimeException ex) {
                errors++;
                // CPF nunca em log (Constituição IV) — apenas a contagem.
                log.warn("Falha ao gerar pagamento para um beneficiário na competência {}",
                        competenceKey, ex);
            }
        }

        // Ciclo já totalmente gerado anteriormente → 409 (REQ-PAY-005).
        if (generated == 0 && preExisting > 0) {
            throw new CycleAlreadyGeneratedException(
                    "Ciclo da competência " + competenceKey + " já foi totalmente gerado");
        }

        return new CycleSummary(competenceKey, beneficiaries.size(),
                generated, ignored, errors, totalGross, totalDiscount, totalNet);
    }

    /**
     * Calcula bruto, desconto e líquido. Nesta feature o desconto é 0 e o líquido
     * iguala o bruto (D3/research — o motor de descontos é feature separada).
     * Líquido nunca negativo (REQ-PAY-007).
     */
    private BenefitBreakdown computeBreakdown(BeneficiarySnapshot beneficiary,
                                              ProgramParameters program,
                                              YearMonth competence) {
        BigDecimal gross = benefitCalculator.calculateGross(beneficiary, program, competence);
        BigDecimal discount = BigDecimal.ZERO.setScale(MONEY_SCALE);
        BigDecimal net = gross.subtract(discount);
        if (net.signum() < 0) {
            net = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.DOWN);
        }
        BigDecimal bonus = BigDecimal.ZERO.setScale(MONEY_SCALE);
        return new BenefitBreakdown(gross, discount, net, bonus);
    }

    /** Valida e converte a competência (REQ-PAY-008 / BR-004 CALCBENF#L141-L144). */
    private YearMonth parseCompetence(String competenceRaw) {
        if (competenceRaw == null || !competenceRaw.matches("\\d{6}")) {
            throw new InvalidCompetenceException("Competência deve ter o formato AAAAMM");
        }
        int year = Integer.parseInt(competenceRaw.substring(0, 4));
        int month = Integer.parseInt(competenceRaw.substring(4, 6));
        if (month < 1 || month > 12) {
            throw new InvalidCompetenceException("Mês da competência deve estar entre 01 e 12");
        }
        return YearMonth.of(year, month);
    }
}
