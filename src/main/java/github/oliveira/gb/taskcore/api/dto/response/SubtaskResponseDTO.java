package github.oliveira.gb.taskcore.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto de resposta contendo os dados da subtarefa")
public record SubtaskResponseDTO(
        @Schema(description = "ID único da subtarefa", example = "1")
        Long id,

        @Schema(description = "Título da subtarefa", example = "Configurar dependências no POM")
        String title,

        @Schema(description = "Indica se a subtarefa foi concluída", example = "false")
        boolean completed
) {}