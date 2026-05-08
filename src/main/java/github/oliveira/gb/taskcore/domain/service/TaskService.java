package github.oliveira.gb.taskcore.domain.service;

import github.oliveira.gb.taskcore.api.dto.request.TaskFilter;
import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.exception.TaskNotFoundException;
import github.oliveira.gb.taskcore.api.mapper.TaskMapper;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import github.oliveira.gb.taskcore.domain.repository.specification.TaskSpecification;
import github.oliveira.gb.taskcore.domain.validation.TaskValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskValidator taskValidator;

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        taskValidator.validateCreation(taskRequestDTO);

        Task task = taskMapper.toEntity(taskRequestDTO);
        task.setStatus(TaskStatus.PENDING);

        var taskEntity = taskRepository.save(task);
        return taskMapper.toResponseDTO(taskEntity);
    }

    @Transactional
    public TaskResponseDTO taskFindById(Long id){
        Task taskFound = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " not found"));

        return taskMapper.toResponseDTO(taskFound);
    }

    @Transactional
    public TaskResponseDTO updateTask(Long id, TaskRequestDTO dto) {
        Task taskEntity = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa com ID " + id + " não encontrada."));

        taskValidator.validateUpdate(taskEntity, dto);
        taskMapper.updateEntityFromDto(dto, taskEntity);
        taskRepository.save(taskEntity);

        return taskMapper.toResponseDTO(taskEntity);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Tarefa com ID " + id + " não encontrada."));

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDTO> findAll(TaskFilter filter, Pageable pageable) {
        Specification<Task> spec = Specification.where((Specification<Task>) null);

        if (filter.text() != null && !filter.text().isBlank()) {
            spec = spec.and(TaskSpecification.hasText(filter.text()));
        }

        if (filter.status() != null) {
            spec = spec.and(TaskSpecification.hasStatus(filter.status()));
        }

        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toResponseDTO);
    }
}