package github.oliveira.gb.taskcore.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto de resposta contendo os dados de uma tag")
public record TagResponseDTO(
        @Schema(description = "ID único da tag", example = "1")
        Long id,

        @Schema(description = "Nome da tag", example = "backend")
        String name
) {}