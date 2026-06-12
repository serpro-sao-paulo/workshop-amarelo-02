package com.datacorp.sifap.beneficiary.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidade JPA para o grupo PE GRP-DEPENDENTE do DDM BENEFICIARIO (DA..DG).
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm}
 * campos DA..DG (PE, max 10 ocorrencias). REQ-005.
 */
@Entity
@Table(name = "dependent")
public class Dependent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dependent_seq")
    @SequenceGenerator(name = "dependent_seq", sequenceName = "dependent_id_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "beneficiary_id", nullable = false)
    private Beneficiary beneficiary;

    /** Indice de ocorrencia do PE (1..10). */
    @Column(name = "occurrence_index", nullable = false)
    private Integer occurrenceIndex;

    /** DB CPF-DEPENDENTE — CPF ou 00000000000. */
    @Column(name = "cpf", length = 11)
    private String cpf;

    /** DC NOME-DEPENDENTE. */
    @Column(name = "name", nullable = false, length = 60)
    private String name;

    /** DD DT-NASC-DEPEND — AAAAMMDD. */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** DE PARENTESCO — FI=Filho CJ=Conjuge NT=Neto TU=Tutelado. */
    @Column(name = "kinship", length = 2)
    private String kinship;

    /** DF SIT-DEPENDENTE — A/I/D. */
    @Column(name = "status", length = 1)
    private String status;

    /** DG IND-DEFICIENCIA — S/N. */
    @Column(name = "disability_flag", length = 1)
    private String disabilityFlag;

    public Long getId() { return id; }
    public Beneficiary getBeneficiary() { return beneficiary; }
    public void setBeneficiary(Beneficiary b) { this.beneficiary = b; }
    public Integer getOccurrenceIndex() { return occurrenceIndex; }
    public void setOccurrenceIndex(Integer i) { this.occurrenceIndex = i; }
    public String getCpf() { return cpf; }
    public void setCpf(String c) { this.cpf = c; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate d) { this.birthDate = d; }
    public String getKinship() { return kinship; }
    public void setKinship(String k) { this.kinship = k; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getDisabilityFlag() { return disabilityFlag; }
    public void setDisabilityFlag(String f) { this.disabilityFlag = f; }
}
