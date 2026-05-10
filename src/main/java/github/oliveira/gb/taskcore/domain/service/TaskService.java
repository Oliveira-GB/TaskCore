package github.oliveira.gb.taskcore.domain.service;

import github.oliveira.gb.taskcore.api.dto.request.TaskFilter;
import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.exception.TaskNotFoundException;
import github.oliveira.gb.taskcore.api.mapper.TaskMapper;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TagRepository;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import github.oliveira.gb.taskcore.domain.repository.specification.TaskSpecification;
import github.oliveira.gb.taskcore.domain.validation.TaskValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final TaskMapper taskMapper;
    private final TaskValidator taskValidator;

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        taskValidator.validateCreation(taskRequestDTO);

        Task task = taskMapper.toEntity(taskRequestDTO);
        task.setStatus(TaskStatus.PENDING);
        task.setTags(mapTags(taskRequestDTO.tags()));

        var taskEntity = taskRepository.save(task);
        return taskMapper.toResponseDTO(taskEntity);
    }

    @Transactional(readOnly = true)
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
        taskEntity.setTags(mapTags(dto.tags()));

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
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();;

        if (filter.text() != null && !filter.text().isBlank()) {
            spec = spec.and(TaskSpecification.hasText(filter.text()));
        }

        if (filter.status() != null) {
            spec = spec.and(TaskSpecification.hasStatus(filter.status()));
        }

        if (filter.tags() != null && !filter.tags().isEmpty()) {
            spec = spec.and(TaskSpecification.hasTags(filter.tags()));
        }

        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toResponseDTO);
    }

    private Set<Tag> mapTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return Collections.emptySet();
        }

        return tagNames.stream()
                .map(name -> tagRepository.findByName(name.toLowerCase().trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(name.toLowerCase().trim());
                            return tagRepository.save(newTag);
                        }))
                .collect(Collectors.toSet());
    }
}