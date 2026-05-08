package github.oliveira.gb.taskcore.api.dto.request;

import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Parâmetros para filtragem dinâmica de tarefas")
public record TaskFilter(

        @Schema(description = "Busca por texto no título ou na descrição", example = "Spring Boot")
        String text,

        @Schema(description = "Filtra pelo status da tarefa", example = "PENDING")
        TaskStatus status,

        @Schema(description = "Lista de nomes de tags para filtragem (OR)", example = "[\"estudo\", \"backend\"]")
        Set<String> tags
) {}