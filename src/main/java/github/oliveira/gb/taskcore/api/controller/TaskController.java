package github.oliveira.gb.taskcore.api.controller;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.exception.ErrorResponseDTO;
import github.oliveira.gb.taskcore.domain.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Endpoints para gerenciamento de tarefas")
public class TaskController implements GenericHeaderLocation {

    private final TaskService taskService;

    @Operation(summary = "Criar uma nova tarefa", description = "Cria uma nova tarefa definindo o status inicial como PENDING.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados (ex: campos nulos ou inválidos)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (ex: título de tarefa já existente)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<TaskResponseDTO> save(@RequestBody @Valid TaskRequestDTO dto){
        var task = taskService.createTask(dto);
        URI location = generateHeaderLocation(task.id());

        return ResponseEntity.created(location).body(task);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> taskFindById(@PathVariable("id") Long id){
        return ResponseEntity.ok(taskService.taskFindById(id));
    }
}