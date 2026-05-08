package github.oliveira.gb.taskcore.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Objeto padronizado de retorno para erros da API")
public record ErrorResponseDTO(

        @Schema(description = "Instante em que o erro ocorreu", example = "2026-05-07T14:30:00Z")
        Instant timestamp,

        @Schema(description = "Código HTTP do erro", example = "400")
        Integer status,

        @Schema(description = "Nome do erro HTTP correspondente ao status", example = "Bad Request")
        String error,

        @Schema(description = "Lista de mensagens detalhando os erros encontrados", example = "[\"title: It is not possible to create a task without a TITLE\"]")
        List<String> messages,

        @Schema(description = "Caminho da URI que gerou o erro", example = "/api/v1/tasks")
        String path
) {
}