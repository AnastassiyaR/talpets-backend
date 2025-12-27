package com.backend;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Базовый класс для интеграционных тестов с Testcontainers.
 *
 * ВАЖНО: Контейнер PostgreSQL создается ОДИН РАЗ для ВСЕХ тестов
 * и переиспользуется между тестовыми классами.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    /**
     * ✅ STATIC контейнер - создается ОДИН РАЗ для ВСЕХ тестовых классов
     * Singleton pattern для Testcontainers
     */
    private static PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>("postgres:14-alpine")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);  // 👈 Важно! Переиспользуем контейнер

        postgresContainer.start();  // 👈 Запускаем сразу
    }

    /**
     * Настройка Spring для подключения к Testcontainers БД
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }
}