package com.datacorp.sifap.beneficiary.domain;

import org.springframework.stereotype.Component;

/**
 * Mascaramento de CPF, traducao FIEL da subrotina {@code MASCARA-CPF}.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L171-L189}
 * <br>Regra: BR-012 / MYS-005.
 *
 * <p><b>AVISO:</b> esta classe reproduz a inconsistencia conhecida do legado.
 * Usada apenas para equivalencia; REQ-006 exige mascara LGPD correta.
 */
@Component
public class CpfMask {

    public String maskLegacy(long cpf) {
        String s = String.format("%011d", cpf); // MOVE numerico N11 -> #CPF-STR (A11)
        if (cpf < 10_000_000_000L) {
            // CPF com menos de 11 digitos: expoe os 3 primeiros (inconsistencia)
            String p1 = s.substring(0, 3);
            return p1 + ".***.***-**";
        }
        // CPF completo: ***.***.XXX-XX (SUBSTR 7,3 e 10,2)
        String p3 = s.substring(6, 9);
        String p4 = s.substring(9, 11);
        return "***.***." + p3 + "-" + p4;
    }
}
