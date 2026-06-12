package com.datacorp.sifap.socialprogram.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidade JPA para PE GRP-PARAM-REGIONAL (FA..FE, max 6) do DDM PROGRAMA-SOCIAL.
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm}
 * campos FA..FE. REQ-007.
 */
@Entity
@Table(name = "program_regional_param")
public class ProgramRegionalParam {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reg_param_seq")
    @SequenceGenerator(name = "reg_param_seq", sequenceName = "reg_param_id_seq", allocationSize = 20)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "social_program_id", nullable = false)
    @JsonBackReference("program-regional")
    private SocialProgram program;

    @Column(name = "occurrence_index", nullable = false)
    private Integer occurrenceIndex;

    /** FB COD-REGIAO — 01-05 ou 99. */
    @Column(name = "region_code", length = 2)
    private String regionCode;

    /** FC FATOR-REGIONAL N(3.4). */
    @Column(name = "regional_factor", precision = 3, scale = 4)
    private BigDecimal regionalFactor;

    /** FD VLR-COMPLEMENTO-REG N(7.2). */
    @Column(name = "regional_complement", precision = 7, scale = 2)
    private BigDecimal regionalComplement;

    /** FE IND-ATIVO-REGIAO — S/N. */
    @Column(name = "active_flag", length = 1)
    private String activeFlag;

    public Long getId() { return id; }
    public SocialProgram getProgram() { return program; }
    public void setProgram(SocialProgram p) { this.program = p; }
    public Integer getOccurrenceIndex() { return occurrenceIndex; }
    public void setOccurrenceIndex(Integer i) { this.occurrenceIndex = i; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String r) { this.regionCode = r; }
    public BigDecimal getRegionalFactor() { return regionalFactor; }
    public void setRegionalFactor(BigDecimal v) { this.regionalFactor = v; }
    public BigDecimal getRegionalComplement() { return regionalComplement; }
    public void setRegionalComplement(BigDecimal v) { this.regionalComplement = v; }
    public String getActiveFlag() { return activeFlag; }
    public void setActiveFlag(String f) { this.activeFlag = f; }
}
