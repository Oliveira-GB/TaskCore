package github.oliveira.gb.taskcore.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Objeto de requisição para a criação de uma subtarefa")
public record SubtaskRequestDTO(
        @Schema(description = "Título da subtarefa", example = "Configurar dependências no POM", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 3, maxLength = 100)
        @NotBlank(message = "Subtask title is mandatory")
        @Size(min = 3, max = 100)
        String title,

        @Schema(description = "Indica se a subtarefa já nasce concluída", example = "false")
        boolean completed
) {}