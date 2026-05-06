package github.oliveira.gb.taskcore.domain.service;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.mapper.TaskMapper;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO taskRequestDTO) {
        Task task = taskMapper.toEntity(taskRequestDTO);
        task.setStatus(TaskStatus.PENDING);

        var taskEntity = taskRepository.save(task);
        return taskMapper.toResponseDTO(taskEntity);
    }
}