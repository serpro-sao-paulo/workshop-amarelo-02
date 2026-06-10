package br.gov.sifap.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Pagamento gerado pelo ciclo mensal (DDM legado {@code PAGAMENTO}).
 *
 * <p>Possuída pelo contexto Payment &amp; Cycle. Persistência via JPA; a tabela é
 * criada pela migration {@code V2__create_payment.sql}.
 */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "beneficiary_cpf", nullable = false, length = 11)
    private String beneficiaryCpf;

    @Column(name = "program_code", nullable = false)
    private int programCode;

    @Column(name = "competence", nullable = false, length = 6)
    private String competence;

    @Column(name = "gross_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "discount_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "net_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "bonus_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal bonusAmount;

    @Column(name = "generation_date", nullable = false)
    private LocalDate generationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 8)
    private PaymentType type;

    /** Construtor exigido pelo JPA. */
    protected Payment() {
    }

    private Payment(UUID id, String beneficiaryCpf, int programCode, String competence,
                    BigDecimal grossAmount, BigDecimal discountAmount, BigDecimal netAmount,
                    BigDecimal bonusAmount, LocalDate generationDate,
                    PaymentStatus status, PaymentType type) {
        this.id = id;
        this.beneficiaryCpf = beneficiaryCpf;
        this.programCode = programCode;
        this.competence = competence;
        this.grossAmount = grossAmount;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
        this.bonusAmount = bonusAmount;
        this.generationDate = generationDate;
        this.status = status;
        this.type = type;
    }

    /**
     * Cria um pagamento mensal regular no estado inicial {@link PaymentStatus#GENERATED}.
     *
     * @param breakdown resultado do cálculo de benefício (bruto, desconto, líquido)
     */
    public static Payment generateNormal(String beneficiaryCpf, int programCode,
                                         String competence, BenefitBreakdown breakdown,
                                         LocalDate generationDate) {
        return new Payment(
                UUID.randomUUID(),
                beneficiaryCpf,
                programCode,
                competence,
                breakdown.grossAmount(),
                breakdown.discountAmount(),
                breakdown.netAmount(),
                breakdown.bonusAmount(),
                generationDate,
                PaymentStatus.GENERATED,
                PaymentType.NORMAL);
    }

    public UUID getId() {
        return id;
    }

    public String getBeneficiaryCpf() {
        return beneficiaryCpf;
    }

    public int getProgramCode() {
        return programCode;
    }

    public String getCompetence() {
        return competence;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public BigDecimal getBonusAmount() {
        return bonusAmount;
    }

    public LocalDate getGenerationDate() {
        return generationDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public PaymentType getType() {
        return type;
    }
}
