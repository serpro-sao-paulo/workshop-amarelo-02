package com.datacorp.sifap.payment.domain;

import java.math.BigDecimal;

/**
 * Item de desconto do PE group {@code DESCONTOS} de BENEFICIARIO (CALCDSCT).
 *
 * @param tipo      C/I/J/S/P/A (CALCDSCT.NSN#L25-L26)
 * @param vlr       valor fixo; se > 0 prevalece sobre o percentual
 * @param pct       percentual (0..100) aplicado sobre o bruto
 * @param dtInicio  vigencia inicial (AAAAMMDD; 0 = sem limite)
 * @param dtFim     vigencia final (AAAAMMDD; 0 = indeterminado)
 */
public record Discount(char tipo, BigDecimal vlr, BigDecimal pct, int dtInicio, int dtFim) {
}
