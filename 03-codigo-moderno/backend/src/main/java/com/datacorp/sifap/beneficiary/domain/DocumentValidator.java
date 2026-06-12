package com.datacorp.sifap.beneficiary.domain;

import java.util.Set;

/**
 * Validacao de documentos, traducao de VALDOCS.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L100-L182}.
 * <br>Regras: BR-010 (backdoor de prefixos de CPF) / MYS-003.
 *
 * <p><b>AVISO DE SEGURANCA:</b> reproduz o BACKDOOR legado — prefixos de CPF na
 * lista especial ({@code 000,001,002,010,011,099,100,999}) forcam documento
 * valido, anulando a validacao de CPF e RG. Mantido apenas para equivalencia.
 * A decisao de preservar ou remover o bypass e do PO/seguranca (MYS-003).
 */
public class DocumentValidator {

    private static final Set<String> PREFIXOS_ESPECIAIS = Set.of(
            "000", "001", "002", "010", "011", "099", "100", "999");

    private final CpfValidator mod11 = new CpfValidator();

    /** VALIDA-CPF-DOC: rejeita CPF=0; senao modulo 11 (VALDOCS.NSN#L103-L145). */
    public boolean isCpfValid(long cpf) {
        if (cpf == 0) {
            return false;
        }
        return mod11.isValid(cpf);
    }

    /** VALIDA-RG: nao vazio e com pelo menos 5 caracteres (VALDOCS.NSN#L148-L165). */
    public boolean isRgValid(String rg) {
        if (rg == null || rg.isBlank()) {
            return false;
        }
        return rg.trim().length() >= 5;
    }

    /** CHECK-DOC-ESPECIAL: backdoor por prefixo de CPF (VALDOCS.NSN#L168-L182). */
    public boolean isSpecialDocument(long cpf) {
        String prefix = String.format("%011d", cpf).substring(0, 3);
        return PREFIXOS_ESPECIAIS.contains(prefix);
    }

    /**
     * Resultado final legado: documento especial forca valido; senao depende de
     * CPF e RG (VALDOCS.NSN#L70-L96).
     */
    public boolean isValidLegacy(long cpf, String rg) {
        if (isSpecialDocument(cpf)) {
            return true; // backdoor: zera erros e forca 'V'
        }
        return isCpfValid(cpf) && isRgValid(rg);
    }
}
