-- V003 - Cria tabelas do contexto Payment & Cycle
-- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm (FNR 152)
-- REQ-009..REQ-023

CREATE SEQUENCE payment_id_seq          START 1 INCREMENT 100;
CREATE SEQUENCE payment_discount_id_seq START 1 INCREMENT 50;

CREATE TABLE payment (
    id                      BIGINT         NOT NULL DEFAULT nextval('payment_id_seq'),
    -- AA NUM-PAGAMENTO sequencial unico
    payment_number          BIGINT         NOT NULL,
    -- AB NUM-CPF (DESCRIPTOR S1)
    cpf                     VARCHAR(11)    NOT NULL,
    registration_number     VARCHAR(11),
    -- AD COD-PROGRAMA (DESCRIPTOR S2)
    program_code            VARCHAR(4)     NOT NULL,
    -- AE ANO-MES-REF AAAAMM (DESCRIPTOR S1)
    competence              VARCHAR(6)     NOT NULL,
    cycle_number            VARCHAR(6),
    -- Valores
    gross_amount            NUMERIC(9,2)   NOT NULL,
    net_amount              NUMERIC(9,2)   NOT NULL,
    total_discount          NUMERIC(7,2),
    -- DA SIT-PAGAMENTO (ADR-0003: G/P/D/E/C/X/R)
    status                  CHAR(1)        NOT NULL DEFAULT 'G',
    -- Tipo de pagamento (campo separado de status para resolver colisao 'D' - ADR-0003)
    -- N=Normal D=Dezembro/13o T=Terceiro
    payment_type            CHAR(1)                 DEFAULT 'N',
    generation_date         DATE,
    emission_date           DATE,
    confirmation_date       DATE,
    cancellation_date       DATE,
    cancellation_reason     CHAR(3),
    -- Dados bancarios
    bank_code               VARCHAR(3),
    agency_code             VARCHAR(6),
    account_number          VARCHAR(13),
    account_type            CHAR(1),
    operation_code          VARCHAR(3),
    -- SIAFI (2002)
    siafi_order_number      VARCHAR(12),
    siafi_commitment_note   VARCHAR(12),
    siafi_management_unit   VARCHAR(6),
    siafi_management_code   VARCHAR(5),
    siafi_integration_status CHAR(1),
    -- Conciliacao bancaria
    reconciliation_date     DATE,
    reconciliation_status   CHAR(1),
    reconciled_amount       NUMERIC(9,2),
    bank_return_code        VARCHAR(2),
    bank_return_description VARCHAR(40),
    -- Hash (2015)
    remittance_hash         VARCHAR(64),
    return_hash             VARCHAR(64),
    -- Controle
    created_at              TIMESTAMP      NOT NULL DEFAULT now(),
    created_by              VARCHAR(8),
    updated_at              TIMESTAMP,
    updated_by              VARCHAR(8),

    CONSTRAINT pk_payment PRIMARY KEY (id),
    CONSTRAINT uq_payment_number UNIQUE (payment_number),
    -- Idempotencia: um pagamento por CPF+competencia (REQ-010)
    CONSTRAINT uq_payment_cpf_comp UNIQUE (cpf, competence),
    CONSTRAINT chk_payment_status CHECK (status IN ('G','P','D','E','C','X','R')),
    CONSTRAINT chk_payment_type   CHECK (payment_type IS NULL OR payment_type IN ('N','D','T'))
);

CREATE INDEX idx_payment_cpf_comp        ON payment (cpf, competence);
CREATE INDEX idx_payment_program_comp_st ON payment (program_code, competence, status);
CREATE INDEX idx_payment_cycle_status    ON payment (cycle_number, status);

-- Grupo PE GRP-DESCONTO (CA..CG, max 8 tipos)
CREATE TABLE payment_discount (
    id               BIGINT       NOT NULL DEFAULT nextval('payment_discount_id_seq'),
    payment_id       BIGINT       NOT NULL,
    occurrence_index INTEGER      NOT NULL,
    -- CB TIPO-DESCONTO IR/JD/CS/PA/EM/TX/OU/EX
    discount_type    VARCHAR(3)   NOT NULL,
    -- CC VLR-DESCONTO N(7.2)
    discount_amount  NUMERIC(7,2),
    -- CD PCT-DESCONTO N(3.2)
    discount_pct     NUMERIC(3,2),
    -- CE NUM-PROCESSO (judicial)
    process_number   VARCHAR(20),
    start_date       DATE,
    end_date         DATE,

    CONSTRAINT pk_payment_discount PRIMARY KEY (id),
    CONSTRAINT fk_payment_discount FOREIGN KEY (payment_id)
        REFERENCES payment(id) ON DELETE CASCADE,
    CONSTRAINT uq_pay_dsct_occ UNIQUE (payment_id, occurrence_index),
    CONSTRAINT chk_pay_dsct_occ CHECK (occurrence_index BETWEEN 1 AND 8)
);
