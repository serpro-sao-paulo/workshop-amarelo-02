package br.gov.sifap.payment.spi;

/**
 * Estado de elegibilidade do beneficiário (subconjunto relevante ao cálculo).
 */
public enum BeneficiaryStatus {
    ACTIVE,
    SUSPENDED,
    BLOCKED,
    INACTIVE
}
