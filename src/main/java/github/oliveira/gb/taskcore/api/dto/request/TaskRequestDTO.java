package github.oliveira.gb.taskcore.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

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
        LocalDateTime dueDate
) {}