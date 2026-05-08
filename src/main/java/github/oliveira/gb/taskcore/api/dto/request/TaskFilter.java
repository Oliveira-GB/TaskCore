package github.oliveira.gb.taskcore.api.dto.request;

import github.oliveira.gb.taskcore.domain.model.TaskStatus;

public record TaskFilter(
        String text,
        TaskStatus status
) {}
