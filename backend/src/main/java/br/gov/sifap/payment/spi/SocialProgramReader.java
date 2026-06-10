package br.gov.sifap.payment.spi;

import java.util.Optional;

/**
 * Porta de leitura para o contexto Social Program.
 *
 * <p>Implementação fornecida por aquele contexto (ou por um adaptador de teste).
 */
public interface SocialProgramReader {

    /**
     * Parâmetros do programa, se existir e estiver ativo (REQ-PAY-004).
     *
     * @param programCode código do programa
     * @return {@link Optional#empty()} quando o programa não existe ou está inativo
     */
    Optional<ProgramParameters> findActiveByCode(int programCode);
}
