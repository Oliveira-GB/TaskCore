package github.oliveira.gb.taskcore.api.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponseDTO(
        Instant timestamp,
        Integer status,
        String error,
        List<String> messages,
        String path
) {
}
