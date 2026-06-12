package com.datacorp.sifap.audit.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Entidade JPA mapeada do DDM AUDITORIA (FNR 153).
 *
 * <p>source_legacy: {@code 01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm}
 * <br>Bounded context: Audit Trail. REQ-018..REQ-020.
 *
 * <p>Campos MU CAMPO-ALTERADO-ANT/DEP + VALOR-ANTERIOR/POSTERIOR (DB/DC/DE/DF)
 * mapeados como JSONB (max 20 ocorrencias cada) — arrays simples nao
 * consultaveis individualmente, o que e aceitavel para trilha de auditoria.
 *
 * <p><b>IMUTAVEL por lei</b> (IN-TCU 63/2010, Art 14 Lei 8159).
 * Retencao minima 10 anos. Este registro nao permite UPDATE/DELETE.
 */
@Entity
@Immutable
@Table(
    name = "audit_event",
    indexes = {
        @Index(name = "idx_audit_date_action",    columnList = "event_date, action_code"),
        @Index(name = "idx_audit_entity_date",    columnList = "entity_type, entity_id, event_date"),
        @Index(name = "idx_audit_user_date",      columnList = "event_user, event_date"),
        @Index(name = "idx_audit_cpf",            columnList = "affected_cpf")
    }
)
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_seq")
    @SequenceGenerator(name = "audit_seq", sequenceName = "audit_event_id_seq", allocationSize = 100)
    private Long id;

    /** AA NUM-AUDITORIA — sequencial unico (DESCRIPTOR). */
    @Column(name = "audit_number", nullable = false, unique = true)
    private Long auditNumber;

    /** AB DT-EVENTO (DESCRIPTOR S1, S2, S3). */
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    /** AC HR-EVENTO HHMMSS. */
    @Column(name = "event_time", nullable = false)
    private LocalTime eventTime;

    /** AD TS-EVENTO — timestamp completo de precisao. */
    @Column(name = "event_timestamp", nullable = false)
    private Long eventTimestamp; // AAAAMMDDHHMMSS como long

    // --- Acao ---

    /** BA COD-ACAO (DESCRIPTOR S1) — IN/AL/EX/CO/LG/LO/BT/ER/AU/RE. */
    @Column(name = "action_code", nullable = false, length = 2)
    private String actionCode;

    /** BB COD-MODULO — nome do programa Natural. */
    @Column(name = "module_code", length = 8)
    private String moduleCode;

    /** BC DES-ACAO. */
    @Column(name = "action_description", length = 80)
    private String actionDescription;

    // --- Entidade afetada ---

    /** CA TIPO-ENTIDADE — BENF/PGTO/PROG/ADMN/SIST (DESCRIPTOR S2). */
    @Column(name = "entity_type", length = 4)
    private String entityType;

    /** CB ID-ENTIDADE (DESCRIPTOR S2). */
    @Column(name = "entity_id", length = 15)
    private String entityId;

    /** CC NUM-CPF-AFETADO (DESCRIPTOR). */
    @Column(name = "affected_cpf", length = 11)
    private String affectedCpf;

    // --- Dados antes/depois (MU max 20 cada, mapeados como JSONB) ---

    /** DB CAMPO-ALTERADO-ANT MU(A30, max 20). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fields_before", columnDefinition = "jsonb")
    private List<String> fieldsBefore;

    /** DC VALOR-ANTERIOR MU(A80, max 20). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "values_before", columnDefinition = "jsonb")
    private List<String> valuesBefore;

    /** DE CAMPO-ALTERADO-DEP MU(A30, max 20). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fields_after", columnDefinition = "jsonb")
    private List<String> fieldsAfter;

    /** DF VALOR-POSTERIOR MU(A80, max 20). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "values_after", columnDefinition = "jsonb")
    private List<String> valuesAfter;

    // --- Usuario e origem ---

    /** EA USR-EVENTO (DESCRIPTOR S3). */
    @Column(name = "event_user", nullable = false, length = 8)
    private String eventUser;

    @Column(name = "user_name", length = 40)
    private String userName;

    /** EC COD-PERFIL — ADM/OPR/CON/AUD/SUP. */
    @Column(name = "user_profile", length = 3)
    private String userProfile;

    @Column(name = "organizational_unit", length = 10)
    private String organizationalUnit;

    @Column(name = "source_ip", length = 15)
    private String sourceIp;

    @Column(name = "session_id", length = 20)
    private String sessionId;

    // --- Contexto batch ---

    @Column(name = "batch_cycle")
    private Long batchCycle;

    @Column(name = "batch_sequence")
    private Long batchSequence;

    @Column(name = "batch_job_name", length = 16)
    private String batchJobName;

    /** FD SIT-BATCH — S/E/W. */
    @Column(name = "batch_status", length = 1)
    private String batchStatus;

    @Column(name = "batch_error_description", length = 120)
    private String batchErrorDescription;

    // --- Correlacao ---

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Column(name = "correlation_sequence")
    private Integer correlationSequence;

    public Long getId() { return id; }
    public Long getAuditNumber() { return auditNumber; }
    public void setAuditNumber(Long n) { this.auditNumber = n; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate d) { this.eventDate = d; }
    public LocalTime getEventTime() { return eventTime; }
    public void setEventTime(LocalTime t) { this.eventTime = t; }
    public Long getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Long ts) { this.eventTimestamp = ts; }
    public String getActionCode() { return actionCode; }
    public void setActionCode(String a) { this.actionCode = a; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String m) { this.moduleCode = m; }
    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String d) { this.actionDescription = d; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String t) { this.entityType = t; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String i) { this.entityId = i; }
    public String getAffectedCpf() { return affectedCpf; }
    public void setAffectedCpf(String c) { this.affectedCpf = c; }
    public List<String> getFieldsBefore() { return fieldsBefore; }
    public void setFieldsBefore(List<String> l) { this.fieldsBefore = l; }
    public List<String> getValuesBefore() { return valuesBefore; }
    public void setValuesBefore(List<String> l) { this.valuesBefore = l; }
    public List<String> getFieldsAfter() { return fieldsAfter; }
    public void setFieldsAfter(List<String> l) { this.fieldsAfter = l; }
    public List<String> getValuesAfter() { return valuesAfter; }
    public void setValuesAfter(List<String> l) { this.valuesAfter = l; }
    public String getEventUser() { return eventUser; }
    public void setEventUser(String u) { this.eventUser = u; }
    public String getUserName() { return userName; }
    public void setUserName(String n) { this.userName = n; }
    public String getUserProfile() { return userProfile; }
    public void setUserProfile(String p) { this.userProfile = p; }
    public String getSourceIp() { return sourceIp; }
    public void setSourceIp(String ip) { this.sourceIp = ip; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String c) { this.correlationId = c; }
}
