-- V001 - Cria tabelas do contexto Beneficiary Management
-- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm (FNR 150)
-- REQ-001..REQ-006

CREATE SEQUENCE beneficiary_id_seq START 1 INCREMENT 50;
CREATE SEQUENCE dependent_id_seq  START 1 INCREMENT 50;

CREATE TABLE beneficiary (
    id                      BIGINT         NOT NULL DEFAULT nextval('beneficiary_id_seq'),
    -- AB NUM-CPF (DESCRIPTOR S1)
    cpf                     VARCHAR(11)    NOT NULL,
    -- AA NUM-INSCRICAO
    registration_number     VARCHAR(11),
    -- AC NOME-COMPLETO
    full_name               VARCHAR(60)    NOT NULL,
    -- AD NOME-MAE (obrigatorio)
    mother_name             VARCHAR(60)    NOT NULL,
    -- AE NOME-PAI (opcional)
    father_name             VARCHAR(60),
    -- AF DT-NASCIMENTO AAAAMMDD
    birth_date              DATE           NOT NULL,
    -- AG SEXO M/F/I
    gender                  CHAR(1),
    -- AH EST-CIVIL S/C/D/V/U
    marital_status          CHAR(1),
    -- AI-AL RG
    rg_number               VARCHAR(15),
    rg_issuer               VARCHAR(10),
    rg_state                CHAR(2),
    rg_issue_date           DATE,
    -- Grupo BA endereco
    street                  VARCHAR(60),
    street_number           VARCHAR(10),
    complement              VARCHAR(30),
    neighborhood            VARCHAR(40),
    city                    VARCHAR(40),
    -- BG UF (DESCRIPTOR S2)
    uf                      CHAR(2),
    zip_code                INTEGER,
    ibge_code               INTEGER,
    -- BJ COD-REGIAO 01-05 ou 99
    region_code             VARCHAR(2),
    -- CA COD-PROGRAMA (DESCRIPTOR S3)
    program_code            VARCHAR(4),
    registration_date       DATE,
    benefit_start_date      DATE,
    benefit_end_date        DATE,
    -- CE SIT-BENEFICIARIO A=Ativo S=Suspenso C=Cancelado I=Inativo D=Desligado
    status                  CHAR(1)        NOT NULL DEFAULT 'A',
    status_reason           CHAR(3),
    status_date             DATE,
    -- CH VLR-RENDA-FAMILIAR N(9.2)
    family_income           NUMERIC(9,2),
    -- CI QTD-MEMBROS-FAMILIA
    family_members          SMALLINT,
    -- CJ IND-RENDA-PERCAP N(7.2)
    per_capita_income       NUMERIC(7,2),
    -- Contato (2015)
    phone_landline          VARCHAR(14),
    phone_mobile            VARCHAR(15),
    email                   VARCHAR(80),
    -- Biometria (2005)
    biometric_status        CHAR(1),
    biometric_collection_date DATE,
    biometric_post_code     VARCHAR(6),
    biometric_hash          VARCHAR(64),
    -- Controle GG NUM-VERSAO (otimistic lock)
    version                 BIGINT         NOT NULL DEFAULT 0,
    created_at              TIMESTAMP      NOT NULL DEFAULT now(),
    created_by              VARCHAR(8),
    updated_at              TIMESTAMP,
    updated_by              VARCHAR(8),

    CONSTRAINT pk_beneficiary PRIMARY KEY (id),
    CONSTRAINT uq_beneficiary_cpf UNIQUE (cpf),
    CONSTRAINT chk_beneficiary_status CHECK (status IN ('A','S','C','I','D')),
    CONSTRAINT chk_beneficiary_gender CHECK (gender IS NULL OR gender IN ('M','F','I'))
);

CREATE INDEX idx_beneficiary_uf_status   ON beneficiary (uf, status);
CREATE INDEX idx_beneficiary_prog_status ON beneficiary (program_code, status);

-- Grupo PE GRP-DEPENDENTE (DA..DG, max 10 ocorrencias)
CREATE TABLE dependent (
    id               BIGINT      NOT NULL DEFAULT nextval('dependent_id_seq'),
    beneficiary_id   BIGINT      NOT NULL,
    occurrence_index INTEGER     NOT NULL,
    cpf              VARCHAR(11),
    name             VARCHAR(60) NOT NULL,
    birth_date       DATE,
    -- DE PARENTESCO FI/CJ/NT/TU
    kinship          VARCHAR(2),
    -- DF SIT-DEPENDENTE A/I/D
    status           CHAR(1),
    -- DG IND-DEFICIENCIA S/N
    disability_flag  CHAR(1),

    CONSTRAINT pk_dependent PRIMARY KEY (id),
    CONSTRAINT fk_dependent_beneficiary FOREIGN KEY (beneficiary_id)
        REFERENCES beneficiary(id) ON DELETE CASCADE,
    CONSTRAINT uq_dependent_occurrence UNIQUE (beneficiary_id, occurrence_index),
    CONSTRAINT chk_dependent_occurrence CHECK (occurrence_index BETWEEN 1 AND 10)
);
