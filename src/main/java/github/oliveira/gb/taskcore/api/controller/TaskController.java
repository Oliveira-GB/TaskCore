package github.oliveira.gb.taskcore.api.controller;

import github.oliveira.gb.taskcore.api.dto.request.TaskFilter;
import github.oliveira.gb.taskcore.api.dto.request.TaskNoteRequestDTO;
import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.request.TaskStatusUpdateRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskNoteResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskSummaryResponseDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    @Operation(summary = "Buscar tarefa por ID", description = "Retorna os detalhes de uma tarefa específica baseada no seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada para o ID informado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> taskFindById(@PathVariable("id") Long id){
        return ResponseEntity.ok(taskService.taskFindById(id));
    }

    @Operation(summary = "Atualizar uma tarefa", description = "Atualiza título, descrição e prazo de uma tarefa existente. " +
            "Não é permitido atualizar tarefas com status COMPLETED ou usar um título já existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada (Tarefa concluída ou título duplicado)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid TaskRequestDTO dto
    ) {
        TaskResponseDTO response = taskService.updateTask(id, dto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Excluir uma tarefa", description = "Realiza a exclusão lógica da tarefa. " +
            "A tarefa deixará de ser listada, mas permanecerá no banco de dados para fins de auditoria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso (Sem conteúdo no corpo)"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada ou já excluída")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar status da tarefa", description = "Atualiza o status da tarefa. " +
            "Quando o status é alterado para COMPLETED, todas as subtarefas são automaticamente marcadas como concluídas (Cascade). " +
            "Ao reabrir uma tarefa (COMPLETED -> PENDING/IN_PROGRESS), as subtarefas mantêm seu status atual (Isolation).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (status nulo ou inválido)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody @Valid TaskStatusUpdateRequestDTO statusDTO) {
        TaskResponseDTO updatedTask = taskService.updateTaskStatus(id, statusDTO);
        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Listar tarefas com filtros e paginação", description = "Retorna uma lista paginada de tarefas. " +
            "Permite filtrar opcionalmente por texto (título/descrição), status e prioridade. " +
            "O padrão é retornar 10 itens por página, ordenados pela data de criação. " +
            "Nota: Este endpoint retorna um resumo sem o campo de progresso e sem notas para evitar problemas de performance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tarefas retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<Page<TaskSummaryResponseDTO>> findAll(
            @org.springdoc.core.annotations.ParameterObject TaskFilter filter,
            @org.springdoc.core.annotations.ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TaskSummaryResponseDTO> response = taskService.findAll(filter, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Adicionar nota à tarefa", description = "Cria uma nova nota associada à tarefa especificada. " +
            "Notes são create-only e não podem ser editadas após a criação.")
    @ApiResponse(responseCode = "201", description = "Nota criada com sucesso")
    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos - conteúdo vazio ou excede 1000 caracteres")
    @PostMapping("/{id}/notes")
    public ResponseEntity<TaskNoteResponseDTO> addNote(
            @PathVariable Long id,
            @RequestBody @Valid TaskNoteRequestDTO dto) {
        TaskNoteResponseDTO note = taskService.addNoteToTask(id, dto);
        return ResponseEntity.status(201).body(note);
    }

    @Operation(summary = "Remover nota da tarefa", description = "Realiza a exclusão lógica (soft delete) da nota. " +
            "A nota não será mais visível mas permanece no banco para fins de auditoria.")
    @ApiResponse(responseCode = "204", description = "Nota removida com sucesso")
    @ApiResponse(responseCode = "404", description = "Tarefa ou nota não encontrada")
    @DeleteMapping("/{id}/notes/{noteId}")
    public ResponseEntity<Void> removeNote(
            @PathVariable Long id,
            @PathVariable Long noteId) {
        taskService.removeNoteFromTask(id, noteId);
        return ResponseEntity.noContent().build();
    }
}