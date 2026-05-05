package github.oliveira.gb.taskcore.api.dto.response;

import github.oliveira.gb.taskcore.domain.model.TaskStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDateTime dueDate,
        Instant createdAt,
        Instant updateAl
) {
}
