package github.oliveira.gb.taskcore.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing task priority levels.
 * Lower weight value means higher priority.
 */
@Getter
@RequiredArgsConstructor
public enum TaskPriority {
    CRITICAL(1),
    HIGH(2),
    MEDIUM(3),
    LOW(4);

    private final int weight;
}
