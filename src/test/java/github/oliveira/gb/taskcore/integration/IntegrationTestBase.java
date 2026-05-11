package github.oliveira.gb.taskcore.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests using Testcontainers Singleton pattern.
 * <p>
 * This class provides a single PostgreSQL container instance shared across all integration tests,
 * preventing the "Connection refused" error caused by Spring Context Caching vs Testcontainers lifecycle mismatch.
 * <p>
 * The container is initialized only once (static singleton) and reused throughout the entire test suite,
 * significantly improving test execution speed and reliability.
 *
 * @see <a href="https://spring.io/blog/2023/06/23/improved-testcontainers-support-in-spring-boot-3-1">Spring Boot 3.1 Testcontainers Support</a>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration")
public abstract class IntegrationTestBase {

    /**
     * Singleton PostgreSQL container instance.
     * <p>
     * Marked with {@code @ServiceConnection} to automatically configure Spring's datasource
     * with the container's connection details (URL, username, password).
     * <p>
     * The container is started once and shared across all test classes extending this base class.
     */
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("taskcore_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    // Static block to ensure container starts before any test runs
    static {
        postgres.start();
    }
}
