package github.oliveira.gb.taskcore.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;


public record TaskRequestDTO(
        @NotBlank(message = "It is not possible to create a task without a TITLE")
        @Size(min = 3, max = 100)
        String title,

        @Size(max = 500)
        String description,

        @FutureOrPresent(message = "Every date must have a due date in the present or future!")
        LocalDateTime dueDate
) {}
