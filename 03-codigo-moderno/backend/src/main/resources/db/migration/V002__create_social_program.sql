-- V002 - Cria tabelas do contexto Social Program
-- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm (FNR 151)
-- REQ-007, REQ-008

CREATE SEQUENCE social_program_id_seq       START 1 INCREMENT 10;
CREATE SEQUENCE calc_tier_id_seq            START 1 INCREMENT 20;
CREATE SEQUENCE reg_param_id_seq            START 1 INCREMENT 20;

CREATE TABLE social_program (
    id                      BIGINT         NOT NULL DEFAULT nextval('social_program_id_seq'),
    -- AA COD-PROGRAMA chave de negocio
    program_code            VARCHAR(4)     NOT NULL,
    name                    VARCHAR(60)    NOT NULL,
    acronym                 VARCHAR(10),
    -- AD TIPO-PROGRAMA A/T/P
    program_type            CHAR(1)        NOT NULL,
    responsible_agency      VARCHAR(10),
    creation_law            VARCHAR(20),
    creation_date           DATE,
    closure_date            DATE,
    -- AI SIT-PROGRAMA A=Ativo I=Inativo E=Encerrado
    status                  CHAR(1)        NOT NULL DEFAULT 'A',
    -- Valores base
    base_value_individual   NUMERIC(7,2),
    base_value_family       NUMERIC(7,2),
    benefit_cap             NUMERIC(9,2),
    benefit_floor           NUMERIC(7,2),
    annual_adjustment_pct   NUMERIC(3,2),
    last_adjustment_date    DATE,
    -- BG FATOR-K N(5.4) - NAO DOCUMENTADO (MYS-002)
    -- FIXME: confirm semantics with SENARC before using in calculation
    factor_k                NUMERIC(5,4),
    -- Elegibilidade
    max_per_capita_income   NUMERIC(7,2),
    min_age                 SMALLINT,
    max_age                 SMALLINT,
    requires_children       CHAR(1),
    min_children            SMALLINT,
    requires_school         CHAR(1),
    requires_vaccination    CHAR(1),
    requires_prenatal       CHAR(1),
    requires_biometrics     CHAR(1),
    -- Controle
    created_at              DATE           NOT NULL DEFAULT current_date,
    created_by              VARCHAR(8),
    updated_at              DATE,
    updated_by              VARCHAR(8),

    CONSTRAINT pk_social_program PRIMARY KEY (id),
    CONSTRAINT uq_social_program_code UNIQUE (program_code),
    CONSTRAINT chk_program_type   CHECK (program_type IN ('A','T','P')),
    CONSTRAINT chk_program_status CHECK (status IN ('A','I','E'))
);

CREATE INDEX idx_prog_type_status ON social_program (program_type, status);

-- PE GRP-FAIXA-CALCULO (DA..DF, max 5)
CREATE TABLE program_calculation_tier (
    id                  BIGINT      NOT NULL DEFAULT nextval('calc_tier_id_seq'),
    social_program_id   BIGINT      NOT NULL,
    occurrence_index    INTEGER     NOT NULL,
    income_from         NUMERIC(7,2),
    income_to           NUMERIC(7,2),
    multiplier_factor   NUMERIC(3,4),
    additional_value    NUMERIC(7,2),
    cumulative_flag     CHAR(1),

    CONSTRAINT pk_calc_tier PRIMARY KEY (id),
    CONSTRAINT fk_calc_tier_prog FOREIGN KEY (social_program_id)
        REFERENCES social_program(id) ON DELETE CASCADE,
    CONSTRAINT uq_calc_tier_occ UNIQUE (social_program_id, occurrence_index),
    CONSTRAINT chk_calc_tier_occ CHECK (occurrence_index BETWEEN 1 AND 5)
);

-- MU TIPO-DSCT-APLIC (EA, max 8)
CREATE TABLE program_discount_types (
    social_program_id   BIGINT      NOT NULL,
    discount_type       VARCHAR(3)  NOT NULL,
    CONSTRAINT pk_prog_dsct PRIMARY KEY (social_program_id, discount_type),
    CONSTRAINT fk_prog_dsct FOREIGN KEY (social_program_id)
        REFERENCES social_program(id) ON DELETE CASCADE
);

-- PE GRP-PARAM-REGIONAL (FA..FE, max 6 regioes)
CREATE TABLE program_regional_param (
    id                  BIGINT      NOT NULL DEFAULT nextval('reg_param_id_seq'),
    social_program_id   BIGINT      NOT NULL,
    occurrence_index    INTEGER     NOT NULL,
    region_code         VARCHAR(2),
    regional_factor     NUMERIC(3,4),
    regional_complement NUMERIC(7,2),
    active_flag         CHAR(1),

    CONSTRAINT pk_reg_param PRIMARY KEY (id),
    CONSTRAINT fk_reg_param_prog FOREIGN KEY (social_program_id)
        REFERENCES social_program(id) ON DELETE CASCADE,
    CONSTRAINT uq_reg_param_occ UNIQUE (social_program_id, occurrence_index),
    CONSTRAINT chk_reg_param_occ CHECK (occurrence_index BETWEEN 1 AND 6)
);
