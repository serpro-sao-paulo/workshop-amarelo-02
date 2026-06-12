package com.datacorp.sifap.socialprogram.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entidade JPA mapeada do DDM PROGRAMA-SOCIAL (FNR 151).
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm}
 * <br>Bounded context: Social Program. REQ-007, REQ-008.
 *
 * <p>PE GRP-FAIXA-CALCULO (DA..DF, max 5) -> {@link ProgramCalculationTier}.<br>
 * PE GRP-PARAM-REGIONAL (FA..FE, max 6) -> {@link ProgramRegionalParam}.<br>
 * MU TIPO-DSCT-APLIC (EA, max 8) -> {@code @ElementCollection} em tabela separada.
 */
@Entity
@Table(
    name = "social_program",
    indexes = {
        @Index(name = "idx_prog_code",        columnList = "program_code", unique = true),
        @Index(name = "idx_prog_type_status",  columnList = "program_type, status")
    }
)
public class SocialProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prog_seq")
    @SequenceGenerator(name = "prog_seq", sequenceName = "social_program_id_seq", allocationSize = 10)
    private Long id;

    /** AA COD-PROGRAMA — chave primaria de negocio (DESCRIPTOR S1). */
    @Column(name = "program_code", nullable = false, length = 4, unique = true)
    private String programCode;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    /** AC SIGLA-PROGRAMA. */
    @Column(name = "acronym", length = 10)
    private String acronym;

    /** AD TIPO-PROGRAMA — A/T/P (DESCRIPTOR S2). */
    @Column(name = "program_type", nullable = false, length = 1)
    private String programType;

    @Column(name = "responsible_agency", length = 10)
    private String responsibleAgency;

    @Column(name = "creation_law", length = 20)
    private String creationLaw;

    @Column(name = "creation_date")
    private LocalDate creationDate;

    @Column(name = "closure_date")
    private LocalDate closureDate;

    /** AI SIT-PROGRAMA — A=Ativo I=Inativo E=Encerrado (DESCRIPTOR S2). */
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    // --- Valores base ---

    /** BA VLR-BASE-INDIVIDUAL N(7.2). */
    @Column(name = "base_value_individual", precision = 7, scale = 2)
    private BigDecimal baseValueIndividual;

    @Column(name = "base_value_family", precision = 7, scale = 2)
    private BigDecimal baseValueFamily;

    @Column(name = "benefit_cap", precision = 9, scale = 2)
    private BigDecimal benefitCap;

    @Column(name = "benefit_floor", precision = 7, scale = 2)
    private BigDecimal benefitFloor;

    @Column(name = "annual_adjustment_pct", precision = 3, scale = 2)
    private BigDecimal annualAdjustmentPct;

    @Column(name = "last_adjustment_date")
    private LocalDate lastAdjustmentDate;

    /**
     * BG FATOR-K N(5.4) — NAO DOCUMENTADO (MYS-002).
     * FIXME: confirm semantics with SENARC before using in calculation.
     */
    @Column(name = "factor_k", precision = 5, scale = 4)
    private BigDecimal factorK;

    // --- Elegibilidade ---

    @Column(name = "max_per_capita_income", precision = 7, scale = 2)
    private BigDecimal maxPerCapitaIncome;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "requires_children", length = 1)
    private String requiresChildren;

    @Column(name = "min_children")
    private Integer minChildren;

    @Column(name = "requires_school", length = 1)
    private String requiresSchool;

    @Column(name = "requires_vaccination", length = 1)
    private String requiresVaccination;

    @Column(name = "requires_prenatal", length = 1)
    private String requiresPrenatal;

    @Column(name = "requires_biometrics", length = 1)
    private String requiresBiometrics;

    // --- PE GRP-FAIXA-CALCULO (DA..DF, max 5) ---

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("program-tiers")
    private List<ProgramCalculationTier> calculationTiers = new ArrayList<>();

    // --- MU TIPO-DSCT-APLIC (EA, max 8) ---

    @ElementCollection
    @CollectionTable(name = "program_discount_types",
            joinColumns = @JoinColumn(name = "social_program_id"))
    @Column(name = "discount_type", length = 3)
    private Set<String> applicableDiscountTypes = new HashSet<>();

    // --- PE GRP-PARAM-REGIONAL (FA..FE, max 6) ---

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("program-regional")
    private List<ProgramRegionalParam> regionalParams = new ArrayList<>();

    // --- Controle ---
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

    @Column(name = "created_by", length = 8, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "updated_by", length = 8)
    private String updatedBy;

    public Long getId() { return id; }
    public String getProgramCode() { return programCode; }
    public void setProgramCode(String c) { this.programCode = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public String getAcronym() { return acronym; }
    public void setAcronym(String a) { this.acronym = a; }
    public String getProgramType() { return programType; }
    public void setProgramType(String t) { this.programType = t; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public BigDecimal getBaseValueIndividual() { return baseValueIndividual; }
    public void setBaseValueIndividual(BigDecimal v) { this.baseValueIndividual = v; }
    public BigDecimal getFactorK() { return factorK; }
    public void setFactorK(BigDecimal f) { this.factorK = f; }
    public BigDecimal getMaxPerCapitaIncome() { return maxPerCapitaIncome; }
    public void setMaxPerCapitaIncome(BigDecimal v) { this.maxPerCapitaIncome = v; }
    public Integer getMinAge() { return minAge; }
    public void setMinAge(Integer a) { this.minAge = a; }
    public Integer getMaxAge() { return maxAge; }
    public void setMaxAge(Integer a) { this.maxAge = a; }
    public List<ProgramCalculationTier> getCalculationTiers() { return calculationTiers; }
    public Set<String> getApplicableDiscountTypes() { return applicableDiscountTypes; }
    public List<ProgramRegionalParam> getRegionalParams() { return regionalParams; }
    public BigDecimal getAnnualAdjustmentPct() { return annualAdjustmentPct; }
    public void setAnnualAdjustmentPct(BigDecimal v) { this.annualAdjustmentPct = v; }
    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate d) { this.creationDate = d; }
    public LocalDate getClosureDate() { return closureDate; }
    public void setClosureDate(LocalDate d) { this.closureDate = d; }
    public String getResponsibleAgency() { return responsibleAgency; }
    public void setResponsibleAgency(String a) { this.responsibleAgency = a; }
    public String getCreationLaw() { return creationLaw; }
    public void setCreationLaw(String l) { this.creationLaw = l; }
    public BigDecimal getBenefitCap() { return benefitCap; }
    public void setBenefitCap(BigDecimal v) { this.benefitCap = v; }
    public BigDecimal getBenefitFloor() { return benefitFloor; }
    public void setBenefitFloor(BigDecimal v) { this.benefitFloor = v; }
    public BigDecimal getBaseValueFamily() { return baseValueFamily; }
    public void setBaseValueFamily(BigDecimal v) { this.baseValueFamily = v; }
    public LocalDate getLastAdjustmentDate() { return lastAdjustmentDate; }
    public void setLastAdjustmentDate(LocalDate d) { this.lastAdjustmentDate = d; }
    public String getRequiresChildren() { return requiresChildren; }
    public void setRequiresChildren(String v) { this.requiresChildren = v; }
    public Integer getMinChildren() { return minChildren; }
    public void setMinChildren(Integer v) { this.minChildren = v; }
    public String getRequiresSchool() { return requiresSchool; }
    public void setRequiresSchool(String v) { this.requiresSchool = v; }
    public String getRequiresVaccination() { return requiresVaccination; }
    public void setRequiresVaccination(String v) { this.requiresVaccination = v; }
    public String getRequiresPrenatal() { return requiresPrenatal; }
    public void setRequiresPrenatal(String v) { this.requiresPrenatal = v; }
    public String getRequiresBiometrics() { return requiresBiometrics; }
    public void setRequiresBiometrics(String v) { this.requiresBiometrics = v; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate d) { this.createdAt = d; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String u) { this.createdBy = u; }
    public LocalDate getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDate d) { this.updatedAt = d; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String u) { this.updatedBy = u; }
}
