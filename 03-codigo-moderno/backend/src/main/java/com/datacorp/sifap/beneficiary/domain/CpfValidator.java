package com.datacorp.sifap.beneficiary.domain;

import org.springframework.stereotype.Component;

/**
 * Validacao de CPF por modulo 11, traduzida da subrotina {@code VALIDA-CPF}.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L224-L269}
 * <br>Regra: BR-002 / REQ-001.
 */
@Component
public class CpfValidator {

    /** Equivalente a {@code PERFORM VALIDA-CPF} retornando {@code #CPF-VALIDO}. */
    public boolean isValid(long cpf) {
        if (cpf < 0 || cpf > 99_999_999_999L) {
            return false;
        }
        int[] dig = digits(cpf);

        // Primeiro digito verificador (CADBENEF.NSN#L240-L252)
        int soma = 0;
        int peso = 10;
        for (int i = 0; i < 9; i++) {
            soma += dig[i] * peso;
            peso--;
        }
        int resto = soma % 11;
        int dv1 = (resto < 2) ? 0 : 11 - resto;
        if (dv1 != dig[9]) {
            return false;
        }

        // Segundo digito verificador (CADBENEF.NSN#L255-L267)
        soma = 0;
        peso = 11;
        for (int i = 0; i < 10; i++) {
            soma += dig[i] * peso;
            peso--;
        }
        resto = soma % 11;
        int dv2 = (resto < 2) ? 0 : 11 - resto;
        return dv2 == dig[10];
    }

    /** Zero-pad para 11 posicoes (equivalente a MOVE #CPF para #CPF-STR A11). */
    private int[] digits(long cpf) {
        String s = String.format("%011d", cpf);
        int[] d = new int[11];
        for (int i = 0; i < 11; i++) {
            d[i] = s.charAt(i) - '0';
        }
        return d;
    }
}
