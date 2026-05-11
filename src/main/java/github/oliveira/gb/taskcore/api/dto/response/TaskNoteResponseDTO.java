package github.oliveira.gb.taskcore.api.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * DTO for task note responses.
 */
@Schema(description = "Resposta contendo dados de uma nota")
public record TaskNoteResponseDTO(
        @Schema(description = "ID único da nota", example = "1")
        Long id,

        @Schema(description = "Conteúdo da nota", example = "Lembrete importante")
        String content,

        @Schema(description = "Data de criação", example = "2026-05-07T14:30:00Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,

        @Schema(description = "Data da última atualização", example = "2026-05-07T14:30:00Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt
) {}
