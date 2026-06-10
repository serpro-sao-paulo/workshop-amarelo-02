package br.gov.sifap.payment.api;

import br.gov.sifap.payment.application.CycleAlreadyGeneratedException;
import br.gov.sifap.payment.application.InvalidCompetenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tradução de exceções do contexto Payment &amp; Cycle para {@link ProblemDetail}.
 */
@RestControllerAdvice(assignableTypes = PaymentCycleController.class)
public class PaymentExceptionHandler {

    @ExceptionHandler({InvalidCompetenceException.class, MethodArgumentNotValidException.class})
    public ProblemDetail handleInvalidCompetence(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Competência inválida");
        problem.setDetail(ex instanceof InvalidCompetenceException
                ? ex.getMessage()
                : "Competência deve ter o formato AAAAMM com mês entre 01 e 12");
        return problem;
    }

    @ExceptionHandler(CycleAlreadyGeneratedException.class)
    public ProblemDetail handleCycleAlreadyGenerated(CycleAlreadyGeneratedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("Ciclo já gerado");
        problem.setDetail(ex.getMessage());
        return problem;
    }
}
