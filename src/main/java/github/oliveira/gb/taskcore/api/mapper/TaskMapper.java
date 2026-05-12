package github.oliveira.gb.taskcore.api.mapper;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TagResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskNoteResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskSummaryResponseDTO;
import github.oliveira.gb.taskcore.domain.model.*;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "archived", expression = "java(requestDTO.archived() != null ? requestDTO.archived() : Boolean.FALSE)")
    Task toEntity(TaskRequestDTO requestDTO);

    @Mapping(target = "progress", expression = "java(calculateProgress(task))")
    @Mapping(target = "notes", expression = "java(toNoteResponseDTOList(task.getNotes()))")
    TaskResponseDTO toResponseDTO(Task task);

    TaskSummaryResponseDTO toSummaryResponseDTO(Task task);

    TagResponseDTO toTagResponseDTO(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", ignore = true)
    void updateEntityFromDto(TaskRequestDTO dto, @MappingTarget Task entity);

    @AfterMapping
    default void linkSubtasks(@MappingTarget Task task) {
        if (task.getSubtasks() != null) {
            task.getSubtasks().forEach(subtask -> subtask.setTask(task));
        }
    }

    @AfterMapping
    default void updateArchivedField(TaskRequestDTO dto, @MappingTarget Task entity) {
        if (dto.archived() != null) {
            entity.setArchived(dto.archived());
        }
    }

    /**
     * Calculates the progress percentage based on completed subtasks.
     * Formula: (completed subtasks / total subtasks) * 100
     * Fallback Rule: If no subtasks, returns 100.00 if task is COMPLETED, otherwise 0.00
     *
     * @param task the task entity
     * @return BigDecimal with 2 decimal places representing completion percentage
     */
    default BigDecimal calculateProgress(Task task) {
        List<Subtask> subtasks = task.getSubtasks();

        // Fallback Rule: No subtasks
        if (subtasks == null || subtasks.isEmpty()) {
            return task.getStatus() == TaskStatus.COMPLETED
                    ? BigDecimal.valueOf(100.00).setScale(2)
                    : BigDecimal.ZERO.setScale(2);
        }

        long total = subtasks.size();
        long completed = subtasks.stream()
                .filter(Subtask::isCompleted)
                .count();

        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * Converts a TaskNote entity to TaskNoteResponseDTO.
     *
     * @param note the task note entity
     * @return TaskNoteResponseDTO
     */
    TaskNoteResponseDTO toNoteResponseDTO(TaskNote note);

    /**
     * Converts a list of TaskNote entities to a list of TaskNoteResponseDTOs.
     *
     * @param notes list of task note entities
     * @return list of TaskNoteResponseDTOs
     */
    default List<TaskNoteResponseDTO> toNoteResponseDTOList(List<TaskNote> notes) {
        if (notes == null) {
            return Collections.emptyList();
        }
        return notes.stream()
                .map(this::toNoteResponseDTO)
                .toList();
    }
}