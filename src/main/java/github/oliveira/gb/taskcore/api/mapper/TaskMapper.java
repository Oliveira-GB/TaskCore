package github.oliveira.gb.taskcore.api.mapper;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TagResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    @Mapping(target = "tags", ignore = true)
    Task toEntity(TaskRequestDTO requestDTO);

    TaskResponseDTO toResponseDTO(Task task);

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
}