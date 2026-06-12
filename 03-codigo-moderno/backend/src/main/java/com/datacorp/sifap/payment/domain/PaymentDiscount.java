package com.datacorp.sifap.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidade JPA para o grupo PE GRP-DESCONTO do DDM PAGAMENTO (CA..CG, max 8).
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm}
 * campos CA..CG. REQ-011.
 */
@Entity
@Table(name = "payment_discount")
public class PaymentDiscount {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pay_discount_seq")
    @SequenceGenerator(name = "pay_discount_seq", sequenceName = "payment_discount_id_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "occurrence_index", nullable = false)
    private Integer occurrenceIndex;

    /** CB TIPO-DESCONTO — IR/JD/CS/PA/EM/TX/OU/EX. */
    @Column(name = "discount_type", nullable = false, length = 3)
    private String discountType;

    /** CC VLR-DESCONTO N(7.2). */
    @Column(name = "discount_amount", precision = 7, scale = 2)
    private BigDecimal discountAmount;

    /** CD PCT-DESCONTO N(3.2). */
    @Column(name = "discount_pct", precision = 3, scale = 2)
    private BigDecimal discountPct;

    /** CE NUM-PROCESSO — numero do processo judicial (se JD). */
    @Column(name = "process_number", length = 20)
    private String processNumber;

    /** CF DT-INICIO-DSCT. */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** CG DT-FIM-DSCT — 0/null = indefinido. */
    @Column(name = "end_date")
    private LocalDate endDate;

    public Long getId() { return id; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment p) { this.payment = p; }
    public Integer getOccurrenceIndex() { return occurrenceIndex; }
    public void setOccurrenceIndex(Integer i) { this.occurrenceIndex = i; }
    public String getDiscountType() { return discountType; }
    public void setDiscountType(String t) { this.discountType = t; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal v) { this.discountAmount = v; }
    public BigDecimal getDiscountPct() { return discountPct; }
    public void setDiscountPct(BigDecimal v) { this.discountPct = v; }
    public String getProcessNumber() { return processNumber; }
    public void setProcessNumber(String p) { this.processNumber = p; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate d) { this.startDate = d; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate d) { this.endDate = d; }
}
