package com.datacorp.sifap;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base para testes de integracao com Testcontainers + PostgreSQL 16.
 * Sobe um unico container compartilhado entre todos os testes.
 */
@SpringBootTest
@Testcontainers
public abstract class SifapTestBase {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> PG =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("sifap_test")
                    .withUsername("sifap")
                    .withPassword("sifap");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      PG::getJdbcUrl);
        registry.add("spring.datasource.username", PG::getUsername);
        registry.add("spring.datasource.password", PG::getPassword);
        registry.add("spring.flyway.enabled",      () -> "true");
    }
}
