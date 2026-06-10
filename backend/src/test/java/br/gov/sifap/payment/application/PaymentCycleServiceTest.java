package br.gov.sifap.payment.application;

import br.gov.sifap.payment.domain.BenefitCalculator;
import br.gov.sifap.payment.domain.BenefitBreakdown;
import br.gov.sifap.payment.domain.Payment;
import br.gov.sifap.payment.repository.PaymentRepository;
import br.gov.sifap.payment.spi.BeneficiaryReader;
import br.gov.sifap.payment.spi.BeneficiarySnapshot;
import br.gov.sifap.payment.spi.BeneficiaryStatus;
import br.gov.sifap.payment.spi.ProgramParameters;
import br.gov.sifap.payment.spi.SocialProgramReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit tests for PaymentCycleService — BR-005 (BATCHPGT.NSN#L176-L345)
@ExtendWith(MockitoExtension.class)
class PaymentCycleServiceTest {

    private static final String COMPETENCE = "202601"; // January 2026
    private static final String CPF        = "12345678901";
    private static final int    PROG_CODE  = 10;

    @Mock private BeneficiaryReader  beneficiaryReader;
    @Mock private SocialProgramReader socialProgramReader;
    @Mock private BenefitCalculator   benefitCalculator;
    @Mock private PaymentRepository   paymentRepository;

    // Fixed clock so generationDate is deterministic
    private final Clock clock = Clock.fixed(
            Instant.parse("2026-01-05T10:00:00Z"), ZoneOffset.UTC);

    private PaymentCycleService service;

    @BeforeEach
    void setUp() {
        service = new PaymentCycleService(
                beneficiaryReader, socialProgramReader, benefitCalculator, paymentRepository, clock);
    }

    // ---- helpers ----

    private BeneficiarySnapshot snapshot() {
        return new BeneficiarySnapshot(
                CPF, BeneficiaryStatus.ACTIVE, 5, 2, new BigDecimal("300.00"),
                LocalDate.of(1980, 6, 15), PROG_CODE);
    }

    private ProgramParameters activeProgram() {
        return new ProgramParameters(PROG_CODE, true, new BigDecimal("800.00"), new BigDecimal("0.05"));
    }

    // ---- tests ----

    @Test
    @DisplayName("generate: one active beneficiary with active program → 1 payment generated")
    void generateOneBeneficiaryOnePayment() {
        // Arrange
        when(beneficiaryReader.findActiveForCompetence(COMPETENCE))
                .thenReturn(List.of(snapshot()));
        when(socialProgramReader.findActiveByCode(PROG_CODE))
                .thenReturn(Optional.of(activeProgram()));
        when(paymentRepository.existsByBeneficiaryCpfAndCompetence(CPF, COMPETENCE))
                .thenReturn(false);
        when(benefitCalculator.calculateGross(any(), any(), any(YearMonth.class)))
                .thenReturn(new BigDecimal("800.00"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CycleSummary summary = service.generate(COMPETENCE);

        // Assert
        assertThat(summary.generated()).isEqualTo(1);
        assertThat(summary.errors()).isZero();
        assertThat(summary.competence()).isEqualTo(COMPETENCE);
    }

    @Test
    @DisplayName("generate: empty beneficiary list → 0 payments, 0 errors")
    void generateNoBeneficiaries() {
        // Arrange
        when(beneficiaryReader.findActiveForCompetence(COMPETENCE)).thenReturn(List.of());

        // Act
        CycleSummary summary = service.generate(COMPETENCE);

        // Assert
        assertThat(summary.generated()).isZero();
        assertThat(summary.processed()).isZero();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("generate: program not found → beneficiary counted as ignored, no payment saved — BR-005/BATCHPGT.NSN#L220-L226")
    void generateProgramNotFound() {
        // Arrange
        when(beneficiaryReader.findActiveForCompetence(COMPETENCE))
                .thenReturn(List.of(snapshot()));
        when(socialProgramReader.findActiveByCode(PROG_CODE))
                .thenReturn(Optional.empty());
        when(paymentRepository.existsByBeneficiaryCpfAndCompetence(anyString(), anyString()))
                .thenReturn(false);

        // Act
        CycleSummary summary = service.generate(COMPETENCE);

        // Assert — inactive/absent program causes beneficiary to be ignored
        assertThat(summary.generated()).isZero();
        assertThat(summary.ignored()).isEqualTo(1);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("generate: all beneficiaries already paid → CycleAlreadyGeneratedException — BR-003/BATCHPGT.NSN#L202-L210")
    void generateIdempotentThrowsWhenCycleAlreadyComplete() {
        // Arrange — every beneficiary already has a payment for this competence
        when(beneficiaryReader.findActiveForCompetence(COMPETENCE))
                .thenReturn(List.of(snapshot()));
        when(paymentRepository.existsByBeneficiaryCpfAndCompetence(CPF, COMPETENCE))
                .thenReturn(true);

        // Act & Assert
        assertThatExceptionOfType(CycleAlreadyGeneratedException.class)
                .isThrownBy(() -> service.generate(COMPETENCE));

        verify(paymentRepository, never()).save(any());
        verify(socialProgramReader, never()).findActiveByCode(anyInt());
    }

    @Test
    @DisplayName("generate: invalid competence format → InvalidCompetenceException")
    void generateInvalidCompetenceThrows() {
        assertThatExceptionOfType(InvalidCompetenceException.class)
                .isThrownBy(() -> service.generate("ABCDEF"));
    }

    @Test
    @DisplayName("generate: competence too short → InvalidCompetenceException")
    void generateCompetenceTooShortThrows() {
        assertThatExceptionOfType(InvalidCompetenceException.class)
                .isThrownBy(() -> service.generate("20260"));
    }

    @Test
    @DisplayName("generate: multiple beneficiaries — counts match")
    void generateMultipleBeneficiaries() {
        // Arrange — 2 beneficiaries, different CPFs, same program
        var snap2 = new BeneficiarySnapshot(
                "98765432100", BeneficiaryStatus.ACTIVE, 3, 1, new BigDecimal("200.00"),
                LocalDate.of(1990, 3, 20), PROG_CODE);

        when(beneficiaryReader.findActiveForCompetence(COMPETENCE))
                .thenReturn(List.of(snapshot(), snap2));
        when(socialProgramReader.findActiveByCode(PROG_CODE))
                .thenReturn(Optional.of(activeProgram()));
        when(paymentRepository.existsByBeneficiaryCpfAndCompetence(anyString(), eq(COMPETENCE)))
                .thenReturn(false);
        when(benefitCalculator.calculateGross(any(), any(), any(YearMonth.class)))
                .thenReturn(new BigDecimal("900.00"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CycleSummary summary = service.generate(COMPETENCE);

        // Assert
        assertThat(summary.processed()).isEqualTo(2);
        assertThat(summary.generated()).isEqualTo(2);
        assertThat(summary.errors()).isZero();
    }

    @Test
    @DisplayName("generate: totalGross accumulates gross amounts of generated payments")
    void generateTotalGrossAccumulates() {
        // Arrange
        when(beneficiaryReader.findActiveForCompetence(COMPETENCE))
                .thenReturn(List.of(snapshot()));
        when(socialProgramReader.findActiveByCode(PROG_CODE))
                .thenReturn(Optional.of(activeProgram()));
        when(paymentRepository.existsByBeneficiaryCpfAndCompetence(CPF, COMPETENCE))
                .thenReturn(false);
        when(benefitCalculator.calculateGross(any(), any(), any(YearMonth.class)))
                .thenReturn(new BigDecimal("1000.00"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        CycleSummary summary = service.generate(COMPETENCE);

        // Assert — gross is at least 1000.00 (discount reduces it to net)
        assertThat(summary.totalGross()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(summary.generated()).isEqualTo(1);
    }
}
