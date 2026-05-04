package github.oliveira.gb.taskcore.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskCoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TaskCore API Reference")
                        .version("1.0")
                        .description("Documentação técnica da API de gerenciamento de tarefas TaskCore."));
    }
}