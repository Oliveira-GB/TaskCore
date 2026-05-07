package github.oliveira.gb.taskcore.domain.service;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.exception.TaskNotFoundException;
import github.oliveira.gb.taskcore.api.mapper.TaskMapper;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import github.oliveira.gb.taskcore.domain.validation.TaskValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<TaskResponseDTO> findAll(Pageable pageable){
        return taskRepository.findAll(pageable).map(taskMapper::toResponseDTO);
    }
}