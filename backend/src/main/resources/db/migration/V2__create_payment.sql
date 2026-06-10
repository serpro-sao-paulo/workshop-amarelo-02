-- V2__create_payment.sql
-- Feature 002-geracao-ciclo-pagamento — contexto Payment & Cycle.
-- Mapeia o DDM legado PAGAMENTO (ver specs/.../data-model.md).
-- Rollback-safe: cria tabela e índices; sem ALTER destrutivo.

CREATE TABLE payment (
    id               UUID         NOT NULL,
    beneficiary_cpf  VARCHAR(11)  NOT NULL,
    program_code     INTEGER      NOT NULL,
    competence       VARCHAR(6)   NOT NULL,
    gross_amount     NUMERIC(11,2) NOT NULL,
    discount_amount  NUMERIC(11,2) NOT NULL DEFAULT 0,
    net_amount       NUMERIC(11,2) NOT NULL,
    bonus_amount     NUMERIC(11,2) NOT NULL DEFAULT 0,
    generation_date  DATE         NOT NULL,
    status           VARCHAR(12)  NOT NULL,
    type             VARCHAR(8)   NOT NULL,
    CONSTRAINT pk_payment PRIMARY KEY (id),
    -- Idempotência do ciclo (REQ-PAY-005 / BR-005)
    CONSTRAINT uq_payment_beneficiary_competence UNIQUE (beneficiary_cpf, competence),
    -- Líquido nunca negativo (REQ-PAY-007); bruto não-negativo
    CONSTRAINT ck_payment_net_non_negative CHECK (net_amount >= 0),
    CONSTRAINT ck_payment_gross_non_negative CHECK (gross_amount >= 0)
);

-- Leitura por ciclo/competência (REQ-PAY-009 e relatórios futuros)
CREATE INDEX idx_payment_competence ON payment (competence);
