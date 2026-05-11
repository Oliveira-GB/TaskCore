package github.oliveira.gb.taskcore.api.dto.request;

import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for task status update requests.
 * Used exclusively for the PATCH /{id}/status endpoint.
 */
@Schema(description = "Requisição para atualização de status da tarefa")
public record TaskStatusUpdateRequestDTO(
        @Schema(description = "Novo status da tarefa", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Status is required")
        TaskStatus status
) {}
