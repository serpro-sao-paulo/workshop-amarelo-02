package br.gov.sifap.payment.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans de infraestrutura do contexto Payment &amp; Cycle.
 */
@Configuration
public class PaymentConfig {

    /** {@link Clock} injetável para tornar a data de geração testável. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
