package com.datacorp.sifap.payment.domain;

/**
 * Traducao dos codigos de status e tipo de pagamento para descricao legivel,
 * traducao de RELPGT.
 *
 * <p>source_legacy:
 * {@code 01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L116-L141}.
 * <br>Regra: BR-020 (modelo de estados G/P/C/D/E). REQ-022.
 *
 * <p>Fidelidade: status {@code 'P'} = PAGO (confirma a decisao do ADR-0003). O
 * tipo {@code 'T'} (TERCEIRO) e traduzido mas nenhum programa o gera (MYS-025).
 */
public final class PaymentDescriptor {

    private PaymentDescriptor() {
    }

    public static String statusDescription(char statusPgto) {
        return switch (statusPgto) {
            case 'G' -> "GERADO";
            case 'P' -> "PAGO";
            case 'C' -> "CANCELAD";
            case 'D' -> "DEVOLVID";
            case 'E' -> "ESTORNAD";
            default -> "OUTRO";
        };
    }

    public static String typeDescription(char tipoPgto) {
        return switch (tipoPgto) {
            case 'N' -> "NORMAL";
            case 'D' -> "DECIMO";
            case 'T' -> "TERCEIRO"; // MYS-025: nenhum programa gera 'T'
            default -> "OUTRO";
        };
    }
}
