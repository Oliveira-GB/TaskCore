package github.oliveira.gb.taskcore.domain.model;

import lombok.Getter;


@Getter
public enum TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED;
}
