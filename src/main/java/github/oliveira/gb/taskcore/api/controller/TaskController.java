package github.oliveira.gb.taskcore.api.controller;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.domain.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.net.URI;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController implements GenericHeaderLocation {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> save(@RequestBody @Valid TaskRequestDTO dto){
        var task = taskService.createTask(dto);
        URI location = generateHeaderLocation(task.id());

        return ResponseEntity.created(location).body(task);
    }
}
