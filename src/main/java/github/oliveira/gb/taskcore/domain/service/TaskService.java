package github.oliveira.gb.taskcore.domain.service;

import github.oliveira.gb.taskcore.api.dto.request.TaskFilter;
import github.oliveira.gb.taskcore.api.dto.request.TaskNoteRequestDTO;
import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.request.TaskStatusUpdateRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskNoteResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskSummaryResponseDTO;
import github.oliveira.gb.taskcore.api.exception.TaskNotFoundException;
import github.oliveira.gb.taskcore.api.mapper.TaskMapper;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskNote;
import github.oliveira.gb.taskcore.domain.model.TaskPriority;
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
        task.setPriority(taskRequestDTO.priority() != null ? taskRequestDTO.priority() : TaskPriority.MEDIUM);
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
        taskEntity.setPriority(dto.priority() != null ? dto.priority() : TaskPriority.MEDIUM);
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

    /**
     * Updates the status of a task with cascade logic for subtasks.
     * Cascade Rule: When transitioning to COMPLETED, all subtasks are marked as completed.
     * Isolation Rule: When reopening (COMPLETED -> PENDING/IN_PROGRESS), subtasks are NOT modified.
     *
     * @param id the task ID
     * @param statusDTO the new status
     * @return TaskResponseDTO with calculated progress
     */
    @Transactional
    public TaskResponseDTO updateTaskStatus(Long id, TaskStatusUpdateRequestDTO statusDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + id + " not found"));

        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = statusDTO.status();

        // Cascade Rule: If transitioning to COMPLETED, mark all subtasks as completed
        if (newStatus == TaskStatus.COMPLETED && oldStatus != TaskStatus.COMPLETED) {
            cascadeCompletionToSubtasks(task);
        }
        // Isolation Rule: If reopening (COMPLETED -> PENDING/IN_PROGRESS), do NOT modify subtasks

        task.setStatus(newStatus);
        Task savedTask = taskRepository.save(task);

        return taskMapper.toResponseDTO(savedTask);
    }

    /**
     * Cascades completion status to all subtasks of a task.
     *
     * @param task the parent task
     */
    private void cascadeCompletionToSubtasks(Task task) {
        if (task.getSubtasks() != null) {
            task.getSubtasks().forEach(subtask -> subtask.setCompleted(true));
        }
    }

    @Transactional(readOnly = true)
    public Page<TaskSummaryResponseDTO> findAll(TaskFilter filter, Pageable pageable) {
        Specification<Task> spec = (root, query, cb) -> cb.conjunction();

        if (filter.text() != null && !filter.text().isBlank()) {
            spec = spec.and(TaskSpecification.hasText(filter.text()));
        }

        if (filter.status() != null) {
            spec = spec.and(TaskSpecification.hasStatus(filter.status()));
        }

        if (filter.tags() != null && !filter.tags().isEmpty()) {
            spec = spec.and(TaskSpecification.hasTags(filter.tags()));
        }

        if (filter.priority() != null) {
            spec = spec.and(TaskSpecification.hasPriority(filter.priority()));
        }

        // Filter for archived tasks - hide archived by default unless includeArchived is true
        if (filter.includeArchived() == null || !filter.includeArchived()) {
            spec = spec.and(TaskSpecification.isArchived(false));
        }

        return taskRepository.findAll(spec, pageable)
                .map(taskMapper::toSummaryResponseDTO);
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

    /**
     * Adds a note to a task.
     * Notes are create-only and cannot be edited after creation.
     *
     * @param taskId the task ID
     * @param dto    the note request
     * @return TaskNoteResponseDTO with the created note
     */
    @Transactional
    public TaskNoteResponseDTO addNoteToTask(Long taskId, TaskNoteRequestDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskId + " not found"));

        TaskNote note = new TaskNote();
        note.setContent(dto.content());
        task.addNote(note);

        taskRepository.save(task);
        return taskMapper.toNoteResponseDTO(note);
    }

    /**
     * Removes a note from a task (soft delete).
     *
     * @param taskId the task ID
     * @param noteId the note ID
     */
    @Transactional
    public void removeNoteFromTask(Long taskId, Long noteId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskId + " not found"));

        TaskNote note = task.getNotes().stream()
                .filter(n -> n.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new TaskNotFoundException("Note with ID " + noteId + " not found for task " + taskId));

        task.removeNote(note);
        taskRepository.save(task);
    }
}