package com.datacorp.sifap.socialprogram.domain;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validacao de elegibilidade, traducao de VALELEG.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L100-L242}.
 * <br>Regras: BR-013 (tipo A/P/T), BR-014 (regiao 99 bypass). REQ-008.
 */
@Component
public class EligibilityValidator {

    private static final BigDecimal RENDA_ASSIST = new BigDecimal("600.00");

    public record Result(boolean eligible, List<String> reasons) {
    }

    /**
     * @param idadeMin  0 = sem limite minimo
     * @param idadeMax  0 = sem limite maximo
     * @param rendaMax  0 = sem teto de renda
     * @param codEleg   codigo de elegibilidade (pode ser nulo/branco)
     * @param docsOk    'S' indica documentacao completa
     */
    public Result validate(int codRegiao, char statusBenef, int idade, BigDecimal renda,
                           int numDep, long nis, char tipoPrograma,
                           int idadeMin, int idadeMax, BigDecimal rendaMax,
                           String codEleg, char docsOk) {
        List<String> reasons = new ArrayList<>();

        // Regiao 99 - bypass total (VALELEG.NSN#L104-L111)
        if (codRegiao == 99) {
            return new Result(true, reasons);
        }

        boolean eligible = true;

        // Status do beneficiario
        if (statusBenef != 'A') {
            eligible = false;
            switch (statusBenef) {
                case 'S' -> reasons.add("BENEFICIARIO SUSPENSO");
                case 'C', 'D' -> reasons.add("BENEFICIARIO CANCELADO/DESLIGADO");
                case 'I' -> reasons.add("BENEFICIARIO INATIVO");
                default -> { }
            }
        }

        // Faixa etaria do programa
        if (idadeMin > 0 && idade < idadeMin) {
            eligible = false;
            reasons.add("IDADE INFERIOR AO MINIMO DO PROGRAMA");
        }
        if (idadeMax > 0 && idade > idadeMax) {
            eligible = false;
            reasons.add("IDADE SUPERIOR AO MAXIMO DO PROGRAMA");
        }

        // Renda maxima do programa
        if (rendaMax != null && rendaMax.compareTo(BigDecimal.ZERO) > 0
                && renda.compareTo(rendaMax) > 0) {
            eligible = false;
            reasons.add("RENDA FAMILIAR ACIMA DO TETO DO PROGRAMA");
        }

        // Elegibilidade por tipo de programa (VALELEG.NSN#L168-L201)
        switch (tipoPrograma) {
            case 'A' -> {
                if (renda.compareTo(RENDA_ASSIST) > 0 && numDep < 1) {
                    eligible = false;
                    reasons.add("PROG ASSISTENCIAL: RENDA > 600 SEM DEPENDENTES");
                }
                if (docsOk != 'S') {
                    eligible = false;
                    reasons.add("DOCUMENTACAO INCOMPLETA");
                }
            }
            case 'P' -> {
                if (idade < 60) {
                    eligible = false;
                    reasons.add("PROG PREVIDENCIARIO: IDADE < 60");
                }
            }
            case 'T' -> {
                if (idade < 16 || idade > 65) {
                    eligible = false;
                    reasons.add("PROG TRABALHO: IDADE FORA DA FAIXA 16-65");
                }
            }
            default -> {
                eligible = false;
                reasons.add("TIPO PROGRAMA DESCONHECIDO");
            }
        }

        // COD-ELEGIBILIDADE especifico (VERIF-ELEG-ESPECIFICA)
        if (codEleg != null && !codEleg.isBlank()) {
            if (codEleg.length() >= 1 && codEleg.charAt(0) == 'R' && nis == 0) {
                eligible = false;
                reasons.add("NIS NAO CADASTRADO");
            }
            if (codEleg.length() >= 2 && codEleg.charAt(1) == 'D' && numDep == 0) {
                eligible = false;
                reasons.add("PROGRAMA REQUER DEPENDENTES");
            }
        }

        return new Result(eligible, reasons);
    }
}
