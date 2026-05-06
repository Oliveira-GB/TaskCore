package github.oliveira.gb.taskcore.api.mapper;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {

    Task toEntity(TaskRequestDTO requestDTO);

    TaskResponseDTO toResponseDTO(Task task);
}