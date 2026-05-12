package github.oliveira.gb.taskcore.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import github.oliveira.gb.taskcore.domain.model.TaskPriority;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for task summary in list views.
 * Excludes progress and subtasks to avoid N+1 queries in paginated results.
 */
@Schema(description = "Resumo da tarefa para listagens")
public record TaskSummaryResponseDTO(
        @Schema(description = "ID único da tarefa", example = "1")
        Long id,

        @Schema(description = "Título da tarefa", example = "Estudar Spring Boot")
        String title,

        @Schema(description = "Descrição detalhada da tarefa", example = "Revisar as anotações do Swagger")
        String description,

        @Schema(description = "Status atual da tarefa", example = "PENDING")
        TaskStatus status,

        @Schema(description = "Nível de prioridade da tarefa", example = "HIGH")
        TaskPriority priority,

        @Schema(description = "Indica se a tarefa está arquivada", example = "false")
        Boolean archived,

        @Schema(description = "Data limite para a conclusão da tarefa", example = "2026-12-31T23:59:59")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dueDate,

        @Schema(description = "Instante em que o registro foi criado", example = "2026-05-07T14:30:00Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,

        @Schema(description = "Instante em que o registro foi atualizado pela última vez", example = "2026-05-07T14:30:00Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt,

        @Schema(description = "Lista de tags associadas")
        Set<TagResponseDTO> tags
) {}
