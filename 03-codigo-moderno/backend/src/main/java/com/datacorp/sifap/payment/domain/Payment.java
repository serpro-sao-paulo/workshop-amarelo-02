package com.datacorp.sifap.payment.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA mapeada do DDM PAGAMENTO (FNR 152).
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm}
 * <br>Bounded context: Payment &amp; Cycle. REQ-009..REQ-023.
 *
 * <p>Grupo PE GRP-DESCONTO (CA..CG, max 8) mapeado como {@link PaymentDiscount}.
 * <br>Status segue a maquina de estados do ADR-0003: G/P/D/E/C/X/R.
 * <br>Tipo de pagamento (DA.TIPO-PGTO do legado, inferred) em campo separado
 * de {@link #status} — resolve colisao da letra 'D' (ADR-0003).
 */
@Entity
@Table(
    name = "payment",
    indexes = {
        @Index(name = "idx_payment_cpf_comp",       columnList = "cpf, competence"),
        @Index(name = "idx_payment_program_comp_st", columnList = "program_code, competence, status"),
        @Index(name = "idx_payment_cycle_status",    columnList = "cycle_number, status")
    }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    @SequenceGenerator(name = "payment_seq", sequenceName = "payment_id_seq", allocationSize = 100)
    private Long id;

    /** AA NUM-PAGAMENTO — sequencial unico (DESCRIPTOR). Gerado pelo banco (payment_number_seq). */
    @Generated(event = EventType.INSERT)
    @Column(name = "payment_number", nullable = false, unique = true, insertable = false, updatable = false)
    private Long paymentNumber;

    /** AB NUM-CPF (DESCRIPTOR S1). */
    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    /** AC NUM-INSCRICAO. */
    @Column(name = "registration_number", length = 11)
    private String registrationNumber;

    /** AD COD-PROGRAMA (DESCRIPTOR S2). */
    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    /** AE ANO-MES-REF AAAAMM (DESCRIPTOR S1). */
    @Column(name = "competence", nullable = false, length = 6)
    private String competence;

    /** AF NUM-CICLO (DESCRIPTOR S3). */
    @Column(name = "cycle_number", length = 6)
    private String cycleNumber;

    // --- Valores ---

    /** BA VLR-BRUTO N(9.2). */
    @Column(name = "gross_amount", nullable = false, precision = 9, scale = 2)
    private BigDecimal grossAmount;

    /** BB VLR-LIQUIDO N(9.2). */
    @Column(name = "net_amount", nullable = false, precision = 9, scale = 2)
    private BigDecimal netAmount;

    /** BC VLR-DESCONTO-TOTAL N(7.2). */
    @Column(name = "total_discount", precision = 7, scale = 2)
    private BigDecimal totalDiscount;

    // --- Grupo PE GRP-DESCONTO (CA..CG, max 8) ---

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "occurrence_index")
    private List<PaymentDiscount> discounts = new ArrayList<>();

    // --- Status e processamento ---

    /**
     * DA SIT-PAGAMENTO — G=Gerado P=Pago D=Devolvido E=Estornado C=Confirmado
     * X=Cancelado R=Reprocessado (ADR-0003 — modelo do codigo legado).
     */
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    /**
     * Tipo de pagamento: N=Normal D=Dezembro/13o T=Terceiro.<br>
     * Campo separado de {@link #status} para resolver colisao da letra 'D' (ADR-0003).
     */
    @Column(name = "payment_type", length = 1)
    private String paymentType;

    @Column(name = "generation_date")
    private LocalDate generationDate;

    @Column(name = "emission_date")
    private LocalDate emissionDate;

    @Column(name = "confirmation_date")
    private LocalDate confirmationDate;

    @Column(name = "cancellation_date")
    private LocalDate cancellationDate;

    @Column(name = "cancellation_reason", length = 3)
    private String cancellationReason;

    // --- Dados bancarios ---

    /** EA COD-BANCO — codigo FEBRABAN. */
    @Column(name = "bank_code", length = 3)
    private String bankCode;

    @Column(name = "agency_code", length = 6)
    private String agencyCode;

    @Column(name = "account_number", length = 13)
    private String accountNumber;

    @Column(name = "account_type", length = 1)
    private String accountType;

    @Column(name = "operation_code", length = 3)
    private String operationCode;

    // --- Integracao SIAFI (2002) ---

    @Column(name = "siafi_order_number", length = 12)
    private String siafiOrderNumber;

    @Column(name = "siafi_commitment_note", length = 12)
    private String siafiCommitmentNote;

    @Column(name = "siafi_management_unit", length = 6)
    private String siafiManagementUnit;

    @Column(name = "siafi_management_code", length = 5)
    private String siafiManagementCode;

    /** FE SIT-INTEG-SIAFI — I=Integrado P=Pend E=Erro. */
    @Column(name = "siafi_integration_status", length = 1)
    private String siafiIntegrationStatus;

    // --- Conciliacao bancaria ---

    @Column(name = "reconciliation_date")
    private LocalDate reconciliationDate;

    /** GB SIT-CONCILIACAO — C=Concil D=Diverg P=Pend N=N/A. */
    @Column(name = "reconciliation_status", length = 1)
    private String reconciliationStatus;

    @Column(name = "reconciled_amount", precision = 9, scale = 2)
    private BigDecimal reconciledAmount;

    /** GD COD-RETORNO-BANCO — CNAB 240. */
    @Column(name = "bank_return_code", length = 2)
    private String bankReturnCode;

    @Column(name = "bank_return_description", length = 40)
    private String bankReturnDescription;

    // --- Hash (2015) ---

    @Column(name = "remittance_hash", length = 64)
    private String remittanceHash;

    @Column(name = "return_hash", length = 64)
    private String returnHash;

    // --- Controle ---

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 8, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 8)
    private String updatedBy;

    @PrePersist
    void onPersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "G"; // REQ-021
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getPaymentNumber() { return paymentNumber; }
    public void setPaymentNumber(Long n) { this.paymentNumber = n; }
    public String getCpf() { return cpf; }
    public void setCpf(String c) { this.cpf = c; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String r) { this.registrationNumber = r; }
    public String getProgramCode() { return programCode; }
    public void setProgramCode(String p) { this.programCode = p; }
    public String getCompetence() { return competence; }
    public void setCompetence(String c) { this.competence = c; }
    public String getCycleNumber() { return cycleNumber; }
    public void setCycleNumber(String c) { this.cycleNumber = c; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal v) { this.grossAmount = v; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal v) { this.netAmount = v; }
    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal v) { this.totalDiscount = v; }
    public List<PaymentDiscount> getDiscounts() { return discounts; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String t) { this.paymentType = t; }
    public LocalDate getGenerationDate() { return generationDate; }
    public void setGenerationDate(LocalDate d) { this.generationDate = d; }
    public LocalDate getEmissionDate() { return emissionDate; }
    public void setEmissionDate(LocalDate d) { this.emissionDate = d; }
    public LocalDate getConfirmationDate() { return confirmationDate; }
    public void setConfirmationDate(LocalDate d) { this.confirmationDate = d; }
    public LocalDate getCancellationDate() { return cancellationDate; }
    public void setCancellationDate(LocalDate d) { this.cancellationDate = d; }
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String r) { this.cancellationReason = r; }
    public String getBankCode() { return bankCode; }
    public void setBankCode(String b) { this.bankCode = b; }
    public String getAgencyCode() { return agencyCode; }
    public void setAgencyCode(String a) { this.agencyCode = a; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String a) { this.accountNumber = a; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String t) { this.accountType = t; }
    public String getBankReturnCode() { return bankReturnCode; }
    public void setBankReturnCode(String c) { this.bankReturnCode = c; }
    public LocalDate getReconciliationDate() { return reconciliationDate; }
    public void setReconciliationDate(LocalDate d) { this.reconciliationDate = d; }
    public String getReconciliationStatus() { return reconciliationStatus; }
    public void setReconciliationStatus(String s) { this.reconciliationStatus = s; }
    public BigDecimal getReconciledAmount() { return reconciledAmount; }
    public void setReconciledAmount(BigDecimal v) { this.reconciledAmount = v; }
    public String getRemittanceHash() { return remittanceHash; }
    public void setRemittanceHash(String h) { this.remittanceHash = h; }
    public String getReturnHash() { return returnHash; }
    public void setReturnHash(String h) { this.returnHash = h; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String c) { this.createdBy = c; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String u) { this.updatedBy = u; }
}
