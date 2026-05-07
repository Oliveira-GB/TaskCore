package github.oliveira.gb.taskcore.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDateTime;

@Schema(description = "Objeto de resposta contendo os dados da tarefa criada")
public record TaskResponseDTO(

        @Schema(description = "ID único da tarefa", example = "1")
        Long id,

        @Schema(description = "Título da tarefa", example = "Estudar Spring Boot")
        String title,

        @Schema(description = "Descrição detalhada da tarefa", example = "Revisar as anotações do Swagger e aplicar no controller")
        String description,

        @Schema(description = "Status atual da tarefa", example = "PENDING")
        TaskStatus status,

        @Schema(description = "Data limite para a conclusão da tarefa", example = "2026-12-31T23:59:59")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dueDate,

        @Schema(description = "Instante em que o registro foi criado", example = "2026-05-07T14:30:00Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,

        @Schema(description = "Instante em que o registro foi atualizado pela última vez", example = "2026-05-07T14:30:00Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt
) {
}