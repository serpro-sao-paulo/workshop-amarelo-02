-- V004 - Cria tabela do contexto Audit Trail
-- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm (FNR 153)
-- Obrigatoriedade legal: IN-TCU 63/2010, Art 14 Lei 8159 (retencao minima 10 anos)
-- REQ-018..REQ-020

CREATE SEQUENCE audit_event_id_seq START 1 INCREMENT 100;

CREATE TABLE audit_event (
    id                      BIGINT         NOT NULL DEFAULT nextval('audit_event_id_seq'),
    -- AA NUM-AUDITORIA
    audit_number            BIGINT         NOT NULL,
    -- AB DT-EVENTO (DESCRIPTOR S1, S2, S3)
    event_date              DATE           NOT NULL,
    -- AC HR-EVENTO HHMMSS
    event_time              TIME           NOT NULL,
    -- AD TS-EVENTO AAAAMMDDHHMMSS (precisao maxima)
    event_timestamp         BIGINT         NOT NULL,
    -- BA COD-ACAO IN/AL/EX/CO/LG/LO/BT/ER/AU/RE
    action_code             VARCHAR(2)     NOT NULL,
    -- BB COD-MODULO nome do programa
    module_code             VARCHAR(8),
    action_description      VARCHAR(80),
    -- CA TIPO-ENTIDADE BENF/PGTO/PROG/ADMN/SIST
    entity_type             VARCHAR(4),
    entity_id               VARCHAR(15),
    -- CC NUM-CPF-AFETADO
    affected_cpf            VARCHAR(11),
    -- DB/DC GRP-ANTES (MU max 20 cada -> JSONB)
    fields_before           JSONB,
    values_before           JSONB,
    -- DE/DF GRP-DEPOIS (MU max 20 cada -> JSONB)
    fields_after            JSONB,
    values_after            JSONB,
    -- EA USR-EVENTO
    event_user              VARCHAR(8)     NOT NULL,
    user_name               VARCHAR(40),
    user_profile            VARCHAR(3),
    organizational_unit     VARCHAR(10),
    source_ip               VARCHAR(15),
    session_id              VARCHAR(20),
    -- Contexto batch
    batch_cycle             BIGINT,
    batch_sequence          BIGINT,
    batch_job_name          VARCHAR(16),
    batch_status            CHAR(1),
    batch_error_description VARCHAR(120),
    -- Correlacao
    correlation_id          VARCHAR(36),
    correlation_sequence    SMALLINT,

    CONSTRAINT pk_audit_event PRIMARY KEY (id),
    CONSTRAINT uq_audit_number UNIQUE (audit_number)
    -- SEM UPDATE/DELETE: registro imutavel por lei
);

CREATE INDEX idx_audit_date_action ON audit_event (event_date, action_code);
CREATE INDEX idx_audit_entity_date ON audit_event (entity_type, entity_id, event_date);
CREATE INDEX idx_audit_user_date   ON audit_event (event_user, event_date);
CREATE INDEX idx_audit_cpf         ON audit_event (affected_cpf);

-- Politica de retencao (comentario DDL para referencia)
-- COMMENT ON TABLE audit_event IS
--   'Registro imutavel. Retencao minima 10 anos (Art 14 Lei 8159 / IN-TCU 63/2010).
--    Acoes EX gravadas (corrige BR-011 / REQ-019). Nenhuma linha pode ser deletada.';
