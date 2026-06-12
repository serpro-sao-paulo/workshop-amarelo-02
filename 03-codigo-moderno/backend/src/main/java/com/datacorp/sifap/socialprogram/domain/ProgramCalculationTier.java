package com.datacorp.sifap.socialprogram.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidade JPA para PE GRP-FAIXA-CALCULO (DA..DF, max 5) do DDM PROGRAMA-SOCIAL.
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm}
 * campos DA..DF. REQ-007.
 */
@Entity
@Table(name = "program_calculation_tier")
public class ProgramCalculationTier {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "calc_tier_seq")
    @SequenceGenerator(name = "calc_tier_seq", sequenceName = "calc_tier_id_seq", allocationSize = 20)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "social_program_id", nullable = false)
    @JsonBackReference("program-tiers")
    private SocialProgram program;

    @Column(name = "occurrence_index", nullable = false)
    private Integer occurrenceIndex;

    /** DB RENDA-INICIO N(7.2). */
    @Column(name = "income_from", precision = 7, scale = 2)
    private BigDecimal incomeFrom;

    /** DC RENDA-FIM N(7.2). */
    @Column(name = "income_to", precision = 7, scale = 2)
    private BigDecimal incomeTo;

    /** DD FATOR-MULTIPLICADOR N(3.4). */
    @Column(name = "multiplier_factor", precision = 3, scale = 4)
    private BigDecimal multiplierFactor;

    /** DE VLR-ADICIONAL N(7.2). */
    @Column(name = "additional_value", precision = 7, scale = 2)
    private BigDecimal additionalValue;

    /** DF IND-ACUMULATIVO — S=acumula com faixa anterior. */
    @Column(name = "cumulative_flag", length = 1)
    private String cumulativeFlag;

    public Long getId() { return id; }
    public SocialProgram getProgram() { return program; }
    public void setProgram(SocialProgram p) { this.program = p; }
    public Integer getOccurrenceIndex() { return occurrenceIndex; }
    public void setOccurrenceIndex(Integer i) { this.occurrenceIndex = i; }
    public BigDecimal getIncomeFrom() { return incomeFrom; }
    public void setIncomeFrom(BigDecimal v) { this.incomeFrom = v; }
    public BigDecimal getIncomeTo() { return incomeTo; }
    public void setIncomeTo(BigDecimal v) { this.incomeTo = v; }
    public BigDecimal getMultiplierFactor() { return multiplierFactor; }
    public void setMultiplierFactor(BigDecimal v) { this.multiplierFactor = v; }
    public BigDecimal getAdditionalValue() { return additionalValue; }
    public void setAdditionalValue(BigDecimal v) { this.additionalValue = v; }
    public String getCumulativeFlag() { return cumulativeFlag; }
    public void setCumulativeFlag(String f) { this.cumulativeFlag = f; }
}
