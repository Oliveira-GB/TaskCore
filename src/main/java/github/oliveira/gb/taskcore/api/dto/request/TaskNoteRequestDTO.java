package github.oliveira.gb.taskcore.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new note attached to a task.
 * Notes are create-only and cannot be edited after creation.
 */
@Schema(description = "Requisição para criação de uma nota")
public record TaskNoteRequestDTO(
        @Schema(description = "Conteúdo da nota", example = "Lembrete importante sobre a tarefa", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 1000)
        @NotBlank(message = "Note content cannot be blank")
        @Size(max = 1000, message = "Note content must not exceed 1000 characters")
        String content
) {}
