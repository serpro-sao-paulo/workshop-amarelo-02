package br.gov.sifap.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.sifap.payment.repository.PaymentRepository;
import br.gov.sifap.payment.spi.BeneficiaryReader;
import br.gov.sifap.payment.spi.BeneficiarySnapshot;
import br.gov.sifap.payment.spi.BeneficiaryStatus;
import br.gov.sifap.payment.spi.ProgramParameters;
import br.gov.sifap.payment.spi.SocialProgramReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Integração do ciclo de pagamento contra PostgreSQL real (Testcontainers).
 * Cobre REQ-PAY-001/003/004/005/009 e a invariante SC-004.
 */
@SpringBootTest
@Testcontainers
class PaymentCycleServiceIT {

    private static final String COMPETENCE = "202606";

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PaymentCycleService service;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void generatesPaymentsAndIgnoresInactiveProgram() {
        CycleSummary summary = service.generate(COMPETENCE);

        // 3 beneficiários no programa ativo (10), 1 no programa inativo (20).
        assertThat(summary.processed()).isEqualTo(4);
        assertThat(summary.generated()).isEqualTo(3);
        assertThat(summary.ignored()).isEqualTo(1);
        assertThat(summary.errors()).isZero();
        // SC-004: invariante de reconciliação.
        assertThat(summary.generated() + summary.ignored() + summary.errors())
                .isEqualTo(summary.processed());
        assertThat(paymentRepository.findByCompetence(COMPETENCE)).hasSize(3);
    }

    @Test
    void rerunIsIdempotentAndReportsConflict() {
        service.generate(COMPETENCE);

        assertThatThrownBy(() -> service.generate(COMPETENCE))
                .isInstanceOf(CycleAlreadyGeneratedException.class);

        // Nenhuma duplicação (REQ-PAY-005).
        assertThat(paymentRepository.findByCompetence(COMPETENCE)).hasSize(3);
    }

    @TestConfiguration
    static class StubReaders {

        @Bean
        BeneficiaryReader beneficiaryReader() {
            return competence -> List.of(
                    snapshot("11111111111", 11, 0, "200.00", 1990, 10),
                    snapshot("22222222222", 6, 3, "700.00", 1956, 10),
                    snapshot("33333333333", 15, 5, "200.00", 2016, 10),
                    snapshot("44444444444", 11, 0, "200.00", 1990, 20));
        }

        @Bean
        SocialProgramReader socialProgramReader() {
            return code -> code == 10
                    ? Optional.of(new ProgramParameters(10, true,
                            new BigDecimal("100.00"), new BigDecimal("0.00")))
                    : Optional.empty();
        }

        private BeneficiarySnapshot snapshot(String cpf, int region, int dependents,
                                             String income, int birthYear, int program) {
            return new BeneficiarySnapshot(cpf, BeneficiaryStatus.ACTIVE, region, dependents,
                    new BigDecimal(income), LocalDate.of(birthYear, 1, 1), program);
        }
    }
}
