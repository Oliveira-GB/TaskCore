package github.oliveira.gb.taskcore.api.dto.request;

import github.oliveira.gb.taskcore.domain.model.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(description = "Objeto de requisição para a criação de uma tarefa")
public record TaskRequestDTO(
        @Schema(description = "Título da tarefa", example = "Estudar Spring Boot", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 100)
        @NotBlank(message = "It is not possible to create a task without a TITLE")
        @Size(min = 3, max = 100)
        String title,

        @Schema(description = "Descrição detalhada da tarefa", example = "Revisar as anotações do Swagger e aplicar no controller", maxLength = 500)
        @Size(max = 500)
        String description,

        @Schema(description = "Data limite para a conclusão da tarefa", example = "2026-12-31T23:59:59")
        @FutureOrPresent(message = "Every date must have a due date in the present or future!")
        LocalDateTime dueDate,

        @Schema(description = "Lista de subtarefas para criação em cascata")
        @Valid
        List<SubtaskRequestDTO> subtasks,

        @Schema(description = "Nomes das tags para associação", example = "[\"estudo\", \"backend\"]")
        Set<String> tags,

        @Schema(description = "Nível de prioridade da tarefa", example = "HIGH")
        TaskPriority priority,

        @Schema(description = "Indica se a tarefa está arquivada", example = "false")
        Boolean archived
) {}