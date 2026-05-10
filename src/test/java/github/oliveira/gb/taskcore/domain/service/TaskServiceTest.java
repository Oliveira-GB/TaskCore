package github.oliveira.gb.taskcore.domain.service;

import github.oliveira.gb.taskcore.api.dto.request.TaskFilter;
import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.api.dto.response.TaskResponseDTO;
import github.oliveira.gb.taskcore.api.exception.TaskNotFoundException;
import github.oliveira.gb.taskcore.api.mapper.TaskMapper;
import github.oliveira.gb.taskcore.domain.exception.BusinessRuleException;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TagRepository;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import github.oliveira.gb.taskcore.domain.repository.specification.TaskSpecification;
import github.oliveira.gb.taskcore.domain.validation.TaskValidator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskValidator taskValidator;

    @InjectMocks
    private TaskService taskService;

    private Task taskEntity;
    private TaskRequestDTO taskRequestDTO;
    private TaskResponseDTO taskResponseDTO;

    @BeforeEach
    void setUp() {
        taskEntity = new Task();
        taskEntity.setId(1L);
        taskEntity.setTitle("Update Client Onboarding Documentation");
        taskEntity.setDescription("Review and update the standard operating procedures for new client onboarding processes.");
        taskEntity.setStatus(TaskStatus.PENDING);
        taskEntity.setDueDate(LocalDateTime.now().plusDays(2));
        taskEntity.setActive(true);
        taskEntity.setSubtasks(Collections.emptyList());
        taskEntity.setTags(Collections.emptySet());

        taskRequestDTO = new TaskRequestDTO(
                "Update Client Onboarding Documentation",
                "Review and update the standard operating procedures for new client onboarding processes.",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet()
        );

        taskResponseDTO = new TaskResponseDTO(
                1L,
                "Update Client Onboarding Documentation",
                "Review and update the standard operating procedures for new client onboarding processes.",
                TaskStatus.PENDING,
                LocalDateTime.now().plusDays(2),
                Instant.now(),
                Instant.now(),
                Collections.emptyList(),
                Collections.emptySet()
        );
    }

    @Test
    @DisplayName("Should inject mocks successfully")
    void sanityCheck() {
        Assertions.assertThat(taskService).isNotNull();
    }

    @Test
    @DisplayName("Should return TaskResponseDTO when task is found by ID")
    void taskFindById_ShouldReturnTaskResponseDTO_WhenSuccessful() {
        // Arrange
        BDDMockito.given(taskRepository.findById(1L))
                .willReturn(Optional.of(taskEntity));
        BDDMockito.given(taskMapper.toResponseDTO(taskEntity))
                .willReturn(taskResponseDTO);

        // Act
        TaskResponseDTO result = taskService.taskFindById(1L);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.id()).isEqualTo(taskResponseDTO.id());
        Assertions.assertThat(result.title()).isEqualTo(taskResponseDTO.title());
        Assertions.assertThat(result.status()).isEqualTo(taskResponseDTO.status());
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when task is not found by ID")
    void taskFindById_ShouldThrowTaskNotFoundException_WhenTaskDoesNotExist() {
        // Arrange
        Long nonExistentId = 99L;
        BDDMockito.given(taskRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> taskService.taskFindById(nonExistentId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with ID " + nonExistentId + " not found");
    }

    @Test
    @DisplayName("Should delete task successfully when task exists")
    void deleteTask_ShouldDeleteTask_WhenTaskExists() {
        // Arrange
        BDDMockito.given(taskRepository.findById(1L))
                .willReturn(Optional.of(taskEntity));

        // Act
        taskService.deleteTask(1L);

        // Assert
        Mockito.verify(taskRepository, Mockito.times(1)).delete(taskEntity);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException and not call delete when task does not exist")
    void deleteTask_ShouldThrowTaskNotFoundException_WhenTaskDoesNotExist() {
        // Arrange
        Long nonExistentId = 99L;
        BDDMockito.given(taskRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> taskService.deleteTask(nonExistentId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Tarefa com ID " + nonExistentId + " não encontrada.");

        Mockito.verify(taskRepository, Mockito.never()).delete(ArgumentMatchers.any(Task.class));
    }


    @Test
    @DisplayName("Should create task successfully with PENDING status when no tags are provided")
    void createTask_ShouldCreateTask_WhenNoTagsProvided() {
        // Arrange
        BDDMockito.willDoNothing().given(taskValidator).validateCreation(taskRequestDTO);
        BDDMockito.given(taskMapper.toEntity(taskRequestDTO)).willReturn(taskEntity);
        BDDMockito.given(taskRepository.save(ArgumentMatchers.any(Task.class))).willReturn(taskEntity);
        BDDMockito.given(taskMapper.toResponseDTO(taskEntity)).willReturn(taskResponseDTO);

        // Act
        TaskResponseDTO result = taskService.createTask(taskRequestDTO);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.title()).isEqualTo(taskRequestDTO.title());
        Assertions.assertThat(taskEntity.getStatus()).isEqualTo(TaskStatus.PENDING);

        Mockito.verify(taskRepository, Mockito.times(1)).save(taskEntity);
        // Garante que o TagRepository nunca foi chamado, já que a requisição não tem tags
        Mockito.verify(tagRepository, Mockito.never()).findByName(ArgumentMatchers.anyString());
    }

    @Test
    @DisplayName("Should create task and save new tags when tags are provided")
    void createTask_ShouldCreateTaskAndSaveTags_WhenTagsProvided() {
        // Arrange
        Set<String> tags = Set.of("backend", "spring");
        TaskRequestDTO requestWithTags = new TaskRequestDTO(
                "Update Client Onboarding Documentation",
                "Review and update the standard operating procedures for new client onboarding processes.",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                tags
        );

        Tag existingTag = new Tag();
        existingTag.setId(1L);
        existingTag.setName("backend");

        Tag newTagSaved = new Tag();
        newTagSaved.setId(2L);
        newTagSaved.setName("spring");

        BDDMockito.willDoNothing().given(taskValidator).validateCreation(requestWithTags);
        BDDMockito.given(taskMapper.toEntity(requestWithTags)).willReturn(taskEntity);

        // Simula que a tag "backend" já existe no banco e "spring" é uma tag nova
        BDDMockito.given(tagRepository.findByName("backend")).willReturn(Optional.of(existingTag));
        BDDMockito.given(tagRepository.findByName("spring")).willReturn(Optional.empty());
        BDDMockito.given(tagRepository.save(ArgumentMatchers.any(Tag.class))).willReturn(newTagSaved);

        BDDMockito.given(taskRepository.save(ArgumentMatchers.any(Task.class))).willReturn(taskEntity);
        BDDMockito.given(taskMapper.toResponseDTO(taskEntity)).willReturn(taskResponseDTO);

        // Act
        TaskResponseDTO result = taskService.createTask(requestWithTags);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Mockito.verify(tagRepository, Mockito.times(1)).findByName("backend");
        Mockito.verify(tagRepository, Mockito.times(1)).findByName("spring");
        // Verifica se o método save do TagRepository foi chamado exatamente 1 vez (apenas para a tag nova)
        Mockito.verify(tagRepository, Mockito.times(1)).save(ArgumentMatchers.any(Tag.class));
        Mockito.verify(taskRepository, Mockito.times(1)).save(taskEntity);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when validation fails during creation")
    void createTask_ShouldThrowBusinessRuleException_WhenValidationFails() {
        // Arrange
        BDDMockito.willThrow(new BusinessRuleException("There is already a task registered with this title."))
                .given(taskValidator).validateCreation(taskRequestDTO);

        // Act & Assert
        Assertions.assertThatThrownBy(() -> taskService.createTask(taskRequestDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("There is already a task registered with this title.");

        // Verifica que o repository NUNCA foi chamado para salvar, pois o processo parou na validação
        Mockito.verify(taskRepository, Mockito.never()).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    @DisplayName("Should update task successfully when valid data is provided")
    void updateTask_ShouldUpdateTaskSuccessfully_WhenValidDataIsProvided() {
        // Arrange
        BDDMockito.given(taskRepository.findById(1L))
                .willReturn(Optional.of(taskEntity));

        BDDMockito.willDoNothing()
                .given(taskValidator).validateUpdate(taskEntity, taskRequestDTO);

        BDDMockito.willDoNothing()
                .given(taskMapper).updateEntityFromDto(taskRequestDTO, taskEntity);

        BDDMockito.given(taskRepository.save(ArgumentMatchers.any(Task.class)))
                .willReturn(taskEntity);

        BDDMockito.given(taskMapper.toResponseDTO(taskEntity))
                .willReturn(taskResponseDTO);

        // Act
        TaskResponseDTO result = taskService.updateTask(1L, taskRequestDTO);

        // Assert
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.title()).isEqualTo(taskResponseDTO.title());

        Mockito.verify(taskRepository, Mockito.times(1)).findById(1L);
        Mockito.verify(taskValidator, Mockito.times(1)).validateUpdate(taskEntity, taskRequestDTO);
        Mockito.verify(taskMapper, Mockito.times(1)).updateEntityFromDto(taskRequestDTO, taskEntity);
        Mockito.verify(taskRepository, Mockito.times(1)).save(taskEntity);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when trying to update a non-existent task")
    void updateTask_ShouldThrowTaskNotFoundException_WhenTaskDoesNotExist() {
        // Arrange
        Long nonExistentId = 99L;
        BDDMockito.given(taskRepository.findById(nonExistentId))
                .willReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThatThrownBy(() -> taskService.updateTask(nonExistentId, taskRequestDTO))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Tarefa com ID " + nonExistentId + " não encontrada.");

        // Verifica que o processo parou antes da validação e do salvamento
        Mockito.verify(taskValidator, Mockito.never()).validateUpdate(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(taskRepository, Mockito.never()).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when trying to update a completed task")
    void updateTask_ShouldThrowBusinessRuleException_WhenTaskIsCompleted() {
        // Arrange
        taskEntity.setStatus(TaskStatus.COMPLETED); // Simulando que a tarefa já está concluída

        BDDMockito.given(taskRepository.findById(1L))
                .willReturn(Optional.of(taskEntity));

        BDDMockito.willThrow(new BusinessRuleException("You cannot edit a task that has already been completed."))
                .given(taskValidator).validateUpdate(taskEntity, taskRequestDTO);

        // Act & Assert
        Assertions.assertThatThrownBy(() -> taskService.updateTask(1L, taskRequestDTO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You cannot edit a task that has already been completed.");

        // Verifica que o processo parou na validação e nunca atualizou ou salvou os dados
        Mockito.verify(taskMapper, Mockito.never()).updateEntityFromDto(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.verify(taskRepository, Mockito.never()).save(ArgumentMatchers.any(Task.class));
    }

    @Test
    @DisplayName("Should return a paginated list of TaskResponseDTO when findAll is called")
    void findAll_ShouldReturnPagedTaskResponseDTO_WhenSuccessful() {
        // Arrange
        TaskFilter filter = new TaskFilter("Update", TaskStatus.PENDING, Set.of("tech"));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(List.of(taskEntity));

        Specification<Task> mockSpec = Mockito.mock(Specification.class);

        // Mockamos as chamadas estáticas para os métodos reais que você criou
        try (var mockedStatic = Mockito.mockStatic(TaskSpecification.class)) {
            mockedStatic.when(() -> TaskSpecification.hasText(filter.text())).thenReturn(mockSpec);
            mockedStatic.when(() -> TaskSpecification.hasStatus(filter.status())).thenReturn(mockSpec);
            mockedStatic.when(() -> TaskSpecification.hasTags(filter.tags())).thenReturn(mockSpec);

            BDDMockito.given(taskRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.eq(pageable)))
                    .willReturn(taskPage);

            BDDMockito.given(taskMapper.toResponseDTO(taskEntity))
                    .willReturn(taskResponseDTO);

            // Act
            Page<TaskResponseDTO> result = taskService.findAll(filter, pageable);

            // Assert
            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getContent()).hasSize(1);

            Mockito.verify(taskRepository, Mockito.times(1))
                    .findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.eq(pageable));
        }
    }
}