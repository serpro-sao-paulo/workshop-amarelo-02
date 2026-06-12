-- V004 - Gera payment_number automaticamente via sequence
-- source_legacy: 01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm (AA NUM-PAGAMENTO)
-- REQ-009: numero de pagamento sequencial unico

CREATE SEQUENCE payment_number_seq START 1 INCREMENT 1;

ALTER TABLE payment
    ALTER COLUMN payment_number SET DEFAULT nextval('payment_number_seq');
