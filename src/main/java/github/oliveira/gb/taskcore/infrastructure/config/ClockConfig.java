package github.oliveira.gb.taskcore.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuração do Clock para garantir uso de UTC em operações temporais.
 * Facilita testes ao permitir mock do bean Clock.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
