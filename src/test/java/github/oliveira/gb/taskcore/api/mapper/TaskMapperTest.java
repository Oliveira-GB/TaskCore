package github.oliveira.gb.taskcore.api.mapper;

import github.oliveira.gb.taskcore.api.dto.request.SubtaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.domain.model.Subtask;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

class TaskMapperTest {

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    @DisplayName("Caminho Feliz: Deve mapear TaskRequestDTO para Entity fielmente")
    void shouldMapRequestDtoToEntity() {
        SubtaskRequestDTO subtaskDto = new SubtaskRequestDTO("Subtask 1", false);
        TaskRequestDTO requestDto = new TaskRequestDTO(
                "Task Title",
                "Task Description",
                LocalDateTime.now().plusDays(5),
                List.of(subtaskDto),
                Set.of("backend")
        );

        Task entity = mapper.toEntity(requestDto);

        Assertions.assertThat(entity.getTitle()).isEqualTo(requestDto.title());
        Assertions.assertThat(entity.getDescription()).isEqualTo(requestDto.description());
        Assertions.assertThat(entity.getDueDate()).isEqualTo(requestDto.dueDate());
        Assertions.assertThat(entity.getSubtasks()).hasSize(1);
        Assertions.assertThat(entity.getSubtasks().get(0).getTitle()).isEqualTo("Subtask 1");
    }

    @Test
    @DisplayName("Caminho Feliz: Deve mapear Entity para TaskResponseDTO incluindo listas")
    void shouldMapEntityToResponseDto() {
        Task task = new Task();
        task.setId(10L);
        task.setTitle("Task Title");
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setCreatedAt(Instant.now());
        task.setUpdatedAt(Instant.now());

        Subtask subtask = new Subtask();
        subtask.setId(1L);
        subtask.setTitle("Subtask 1");
        subtask.setCompleted(true);
        task.addSubtask(subtask);

        Tag tag = new Tag();
        tag.setId(5L);
        tag.setName("java");
        task.addTag(tag);

        TaskResponseDTO responseDto = mapper.toResponseDTO(task);

        Assertions.assertThat(responseDto.id()).isEqualTo(10L);
        Assertions.assertThat(responseDto.title()).isEqualTo("Task Title");
        Assertions.assertThat(responseDto.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        Assertions.assertThat(responseDto.createdAt()).isEqualTo(task.getCreatedAt());
        Assertions.assertThat(responseDto.subtasks()).hasSize(1);
        Assertions.assertThat(responseDto.subtasks().get(0).title()).isEqualTo("Subtask 1");
        Assertions.assertThat(responseDto.subtasks().get(0).completed()).isTrue();
        Assertions.assertThat(responseDto.tags()).hasSize(1);
        Assertions.assertThat(responseDto.tags().iterator().next().name()).isEqualTo("java");
    }

    @Test
    @DisplayName("Casos de Borda: Deve evitar NullPointerException com listas nulas e campos opcionais ausentes")
    void shouldHandleNullListsAndOptionalFieldsGracefully() {
        TaskRequestDTO requestDto = new TaskRequestDTO(
                "Minimal Task",
                null, // descrição opcional
                null,
                null, // subtasks nulas
                null  // tags nulas
        );

        // Action
        Task entity = mapper.toEntity(requestDto);

        // Assertions
        Assertions.assertThat(entity.getTitle()).isEqualTo("Minimal Task");
        Assertions.assertThat(entity.getDescription()).isNull();
        Assertions.assertThat(entity.getSubtasks()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Lógica Especializada: @AfterMapping deve vincular a referência da Task nas Subtasks")
    void shouldLinkSubtasksToParentTaskAfterMapping() {
        SubtaskRequestDTO subtaskDto = new SubtaskRequestDTO("Subtask linking test", false);
        TaskRequestDTO requestDto = new TaskRequestDTO(
                "Parent Task",
                null,
                null,
                List.of(subtaskDto),
                null
        );

        // Action
        Task entity = mapper.toEntity(requestDto);

        // Assertions
        Assertions.assertThat(entity.getSubtasks()).hasSize(1);
        Assertions.assertThat(entity.getSubtasks().get(0).getTask()).isEqualTo(entity);
    }

    @Test
    @DisplayName("Lógica Especializada: updateEntityFromDto deve ignorar ID, Status, Timestamps e Tags")
    void shouldUpdateEntityIgnoringProtectedFields() {
        Task existingEntity = new Task();
        existingEntity.setId(99L);
        existingEntity.setTitle("Old Title");
        existingEntity.setStatus(TaskStatus.PENDING);
        Instant originalTime = Instant.now().minusSeconds(3600);
        existingEntity.setCreatedAt(originalTime);
        existingEntity.setUpdatedAt(originalTime);

        Tag existingTag = new Tag(1L, "database", true);
        existingEntity.addTag(existingTag);

        TaskRequestDTO updateDto = new TaskRequestDTO(
                "New Title Updated",
                "New Description Updated",
                LocalDateTime.now().plusDays(2),
                null,
                Set.of("hacked-tag") // Tentativa de sobrescrever tags
        );

        // Action
        mapper.updateEntityFromDto(updateDto, existingEntity);

        // Assertions: O que DEVE mudar
        Assertions.assertThat(existingEntity.getTitle()).isEqualTo("New Title Updated");
        Assertions.assertThat(existingEntity.getDescription()).isEqualTo("New Description Updated");

        // Assertions: O que NÃO PODE mudar (Políticas de ignorar do Mapper)
        Assertions.assertThat(existingEntity.getId()).isEqualTo(99L);
        Assertions.assertThat(existingEntity.getStatus()).isEqualTo(TaskStatus.PENDING);
        Assertions.assertThat(existingEntity.getCreatedAt()).isEqualTo(originalTime);
        Assertions.assertThat(existingEntity.getUpdatedAt()).isEqualTo(originalTime);
        Assertions.assertThat(existingEntity.getTags()).hasSize(1);
        Assertions.assertThat(existingEntity.getTags().iterator().next().getName()).isEqualTo("database");
    }

    @Test
    @DisplayName("Lógica Especializada: toEntity deve ignorar a lista de tags")
    void shouldIgnoreTagsWhenMappingToEntity() {
        // Setup
        TaskRequestDTO requestDto = new TaskRequestDTO(
                "Task with Tags",
                null,
                null,
                null,
                Set.of("tag1", "tag2") // DTO possui tags
        );

        // Action
        Task entity = mapper.toEntity(requestDto);

        Assertions.assertThat(entity.getTags()).isNullOrEmpty();
    }
}