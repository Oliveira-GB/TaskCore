package github.oliveira.gb.taskcore.domain.validation;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.domain.exception.BusinessRuleException;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskValidator {

    private final TaskRepository taskRepository;

    public void validateCreation(TaskRequestDTO dto) {
        if (taskRepository.existsByTitleIgnoreCase(dto.title())) {
            throw new BusinessRuleException("Já existe uma tarefa cadastrada com este título.");
        }
    }
}