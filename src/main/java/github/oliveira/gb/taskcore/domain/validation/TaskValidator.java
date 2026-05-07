package github.oliveira.gb.taskcore.domain.validation;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.domain.exception.BusinessRuleException;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskValidator {

    private final TaskRepository taskRepository;

    public void validateCreation(TaskRequestDTO dto) {
        if (checkTitleDuplicate(dto.title(), null)) {
            throw new BusinessRuleException("There is already a task registered with this title.");
        }
    }

    public void validateUpdate(Task task, TaskRequestDTO dto) {
        if (TaskStatus.COMPLETED.equals(task.getStatus())) {
            throw new BusinessRuleException("You cannot edit a task that has already been completed.");
        }

        if (checkTitleDuplicate(dto.title(), task.getId())) {
            throw new BusinessRuleException("There is already a task registered with this title.");
        }
    }

    private boolean checkTitleDuplicate(String title, Long idParaIgnorar) {
        return taskRepository.findByTitleIgnoreCase(title)
                .map(taskEncontrada -> !taskEncontrada.getId().equals(idParaIgnorar))
                .orElse(false);
    }
}