package com.datacorp.sifap.beneficiary.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA mapeada do DDM BENEFICIARIO (FNR 150, Adabas file 150).
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm}
 * <br>Bounded context: Beneficiary Management. REQ-001..REQ-006.
 *
 * <p>Grupo PE GRP-DEPENDENTE (DA..DG, max 10) mapeado como {@code @OneToMany}
 * em tabela separada {@link Dependent}.
 */
@Entity
@Table(
    name = "beneficiary",
    indexes = {
        @Index(name = "idx_beneficiary_cpf",        columnList = "cpf", unique = true),
        @Index(name = "idx_beneficiary_uf_status",  columnList = "uf, status"),
        @Index(name = "idx_beneficiary_prog_status", columnList = "program_code, status")
    }
)
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiary_seq")
    @SequenceGenerator(name = "beneficiary_seq", sequenceName = "beneficiary_id_seq", allocationSize = 50)
    private Long id;

    /** AB NUM-CPF — CPF sem formatacao (DESCRIPTOR). */
    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    /** AA NUM-INSCRICAO — matricula / ISN alternativo. */
    @Column(name = "registration_number", length = 11)
    private String registrationNumber;

    /** AC NOME-COMPLETO. */
    @Column(name = "full_name", nullable = false, length = 60)
    private String fullName;

    /** AD NOME-MAE — obrigatorio. */
    @Column(name = "mother_name", nullable = false, length = 60)
    private String motherName;

    /** AE NOME-PAI — opcional. */
    @Column(name = "father_name", length = 60)
    private String fatherName;

    /** AF DT-NASCIMENTO — AAAAMMDD no legado; armazenado como LocalDate. */
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    /** AG SEXO — M/F/I. */
    @Column(name = "gender", length = 1)
    private String gender;

    /** AH EST-CIVIL — S/C/D/V/U. */
    @Column(name = "marital_status", length = 1)
    private String maritalStatus;

    /** AI RG-NUMERO. */
    @Column(name = "rg_number", length = 15)
    private String rgNumber;

    /** AJ RG-ORGAO. */
    @Column(name = "rg_issuer", length = 10)
    private String rgIssuer;

    /** AK RG-UF. */
    @Column(name = "rg_state", length = 2)
    private String rgState;

    /** AL RG-DT-EXPEDICAO — AAAAMMDD. */
    @Column(name = "rg_issue_date")
    private LocalDate rgIssueDate;

    // --- Grupo endereco (BA group — campos inline, nao PE) ---

    @Column(name = "street", length = 60)
    private String street;

    @Column(name = "street_number", length = 10)
    private String streetNumber;

    @Column(name = "complement", length = 30)
    private String complement;

    @Column(name = "neighborhood", length = 40)
    private String neighborhood;

    @Column(name = "city", length = 40)
    private String city;

    /** BG UF (DESCRIPTOR S2). */
    @Column(name = "uf", length = 2)
    private String uf;

    @Column(name = "zip_code")
    private Integer zipCode;

    /** BI COD-IBGE. */
    @Column(name = "ibge_code")
    private Integer ibgeCode;

    /** BJ COD-REGIAO — 01-05 ou 99 (especial). */
    @Column(name = "region_code", length = 2)
    private String regionCode;

    // --- Dados do beneficio ---

    /** CA COD-PROGRAMA (DESCRIPTOR S3). */
    @Column(name = "program_code", length = 4)
    private String programCode;

    /** CB DT-CADASTRO. */
    @Column(name = "registration_date")
    private LocalDate registrationDate;

    /** CC DT-INICIO-BENEF. */
    @Column(name = "benefit_start_date")
    private LocalDate benefitStartDate;

    /** CD DT-FIM-BENEF — 0 = sem prazo. */
    @Column(name = "benefit_end_date")
    private LocalDate benefitEndDate;

    /**
     * CE SIT-BENEFICIARIO — A=Ativo S=Suspenso C=Cancelado I=Inativo D=Desligado.
     * <br>DESCRIPTOR S2, S3. Maquina de estados definida em ADR-0003.
     */
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    /** CF MOT-SITUACAO — codigo de motivo (tabela interna). */
    @Column(name = "status_reason", length = 3)
    private String statusReason;

    /** CG DT-ULT-SITUACAO. */
    @Column(name = "status_date")
    private LocalDate statusDate;

    /** CH VLR-RENDA-FAMILIAR N(9.2). */
    @Column(name = "family_income", precision = 9, scale = 2)
    private BigDecimal familyIncome;

    /** CI QTD-MEMBROS-FAMILIA. */
    @Column(name = "family_members")
    private Integer familyMembers;

    /** CJ IND-RENDA-PERCAP N(7.2). */
    @Column(name = "per_capita_income", precision = 7, scale = 2)
    private BigDecimal perCapitaIncome;

    // --- Grupo PE GRP-DEPENDENTE (DA..DG, max 10) ---

    @OneToMany(mappedBy = "beneficiary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "occurrence_index")
    private List<Dependent> dependents = new ArrayList<>();

    // --- Contato (2015) ---

    @Column(name = "phone_landline", length = 14)
    private String phoneLandline;

    @Column(name = "phone_mobile", length = 15)
    private String phoneMobile;

    @Column(name = "email", length = 80)
    private String email;

    // --- Biometria (2005) ---

    /** FA IND-BIOMETRIA — S/N/P. */
    @Column(name = "biometric_status", length = 1)
    private String biometricStatus;

    @Column(name = "biometric_collection_date")
    private LocalDate biometricCollectionDate;

    @Column(name = "biometric_post_code", length = 6)
    private String biometricPostCode;

    /** FD HASH-DIGITAL — SHA-256 ("NAO IMPL" no DDM). */
    @Column(name = "biometric_hash", length = 64)
    private String biometricHash;

    // --- Controle interno ---

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 8, updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 8)
    private String updatedBy;

    /** GG NUM-VERSAO — controle de concorrencia otimista. */
    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    void onPersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "A";
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getters / setters ---
    public Long getId() { return id; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String s) { this.maritalStatus = s; }
    public String getRgNumber() { return rgNumber; }
    public void setRgNumber(String r) { this.rgNumber = r; }
    public String getRgIssuer() { return rgIssuer; }
    public void setRgIssuer(String r) { this.rgIssuer = r; }
    public String getRgState() { return rgState; }
    public void setRgState(String r) { this.rgState = r; }
    public LocalDate getRgIssueDate() { return rgIssueDate; }
    public void setRgIssueDate(LocalDate d) { this.rgIssueDate = d; }
    public String getStreet() { return street; }
    public void setStreet(String s) { this.street = s; }
    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String s) { this.streetNumber = s; }
    public String getComplement() { return complement; }
    public void setComplement(String s) { this.complement = s; }
    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String s) { this.neighborhood = s; }
    public String getCity() { return city; }
    public void setCity(String s) { this.city = s; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public Integer getZipCode() { return zipCode; }
    public void setZipCode(Integer z) { this.zipCode = z; }
    public Integer getIbgeCode() { return ibgeCode; }
    public void setIbgeCode(Integer i) { this.ibgeCode = i; }
    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String r) { this.regionCode = r; }
    public String getProgramCode() { return programCode; }
    public void setProgramCode(String p) { this.programCode = p; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate d) { this.registrationDate = d; }
    public LocalDate getBenefitStartDate() { return benefitStartDate; }
    public void setBenefitStartDate(LocalDate d) { this.benefitStartDate = d; }
    public LocalDate getBenefitEndDate() { return benefitEndDate; }
    public void setBenefitEndDate(LocalDate d) { this.benefitEndDate = d; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusReason() { return statusReason; }
    public void setStatusReason(String s) { this.statusReason = s; }
    public LocalDate getStatusDate() { return statusDate; }
    public void setStatusDate(LocalDate d) { this.statusDate = d; }
    public BigDecimal getFamilyIncome() { return familyIncome; }
    public void setFamilyIncome(BigDecimal v) { this.familyIncome = v; }
    public Integer getFamilyMembers() { return familyMembers; }
    public void setFamilyMembers(Integer v) { this.familyMembers = v; }
    public BigDecimal getPerCapitaIncome() { return perCapitaIncome; }
    public void setPerCapitaIncome(BigDecimal v) { this.perCapitaIncome = v; }
    public List<Dependent> getDependents() { return dependents; }
    public String getPhoneLandline() { return phoneLandline; }
    public void setPhoneLandline(String p) { this.phoneLandline = p; }
    public String getPhoneMobile() { return phoneMobile; }
    public void setPhoneMobile(String p) { this.phoneMobile = p; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getBiometricStatus() { return biometricStatus; }
    public void setBiometricStatus(String b) { this.biometricStatus = b; }
    public LocalDate getBiometricCollectionDate() { return biometricCollectionDate; }
    public void setBiometricCollectionDate(LocalDate d) { this.biometricCollectionDate = d; }
    public String getBiometricPostCode() { return biometricPostCode; }
    public void setBiometricPostCode(String c) { this.biometricPostCode = c; }
    public String getBiometricHash() { return biometricHash; }
    public void setBiometricHash(String h) { this.biometricHash = h; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String c) { this.createdBy = c; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String u) { this.updatedBy = u; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String r) { this.registrationNumber = r; }
}
