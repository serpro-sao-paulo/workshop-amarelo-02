package com.datacorp.sifap.beneficiary.domain;

import org.springframework.stereotype.Component;

/**
 * Define o status inicial do beneficiario na inclusao.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L160-L169}
 * <br>Regra: BR-016 ("AJUSTE STATUS IDOSO 2011").
 */
@Component
public class BeneficiaryStatusRule {

    public char initialStatus(int dtNascimento, int anoAtual) {
        int anoNasc = dtNascimento / 10000;
        int idade = anoAtual - anoNasc;
        char status = 'A';
        if (idade > 75) {
            status = 'S';
        }
        return status;
    }
}
