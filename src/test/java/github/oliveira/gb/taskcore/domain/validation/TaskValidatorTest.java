package github.oliveira.gb.taskcore.domain.validation;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.domain.exception.BusinessRuleException;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TaskValidatorTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskValidator taskValidator;

    @Test
    @DisplayName("Caminho Feliz: Não deve lançar exceção quando o título é único")
    void shouldPassValidationWhenTitleIsUnique() {
        // Setup
        String title = "New Unique Task";
        TaskRequestDTO dto = new TaskRequestDTO(title, "Description", null, null, null);

        // Simula que o banco de dados não encontrou nenhuma tarefa com esse título
        given(taskRepository.findByTitleIgnoreCase(title)).willReturn(Optional.empty());

        // Action & Assertions: Valida que a execução ocorre sem lançar nenhum erro
        Assertions.assertThatCode(() -> taskValidator.validateCreation(dto))
                .doesNotThrowAnyException();

        // Verification
        then(taskRepository).should(times(1)).findByTitleIgnoreCase(title);
    }

    @Test
    @DisplayName("Cenário de Erro: Deve lançar BusinessRuleException quando título já existe na criação")
    void shouldThrowExceptionWhenTitleIsDuplicatedOnCreation() {
        // Setup
        String title = "Existing Task";
        TaskRequestDTO dto = new TaskRequestDTO(title, "Description", null, null, null);

        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle(title);

        // Simula que o banco encontrou uma tarefa que já possui esse título
        given(taskRepository.findByTitleIgnoreCase(title)).willReturn(Optional.of(existingTask));

        // Action & Assertions: Valida a exceção exata e a mensagem de erro
        Assertions.assertThatThrownBy(() -> taskValidator.validateCreation(dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("There is already a task registered with this title.");

        // Verification
        then(taskRepository).should(times(1)).findByTitleIgnoreCase(title);
    }

    @Test
    @DisplayName("Cenário de Erro: Deve lançar exceção ao tentar editar uma tarefa concluída")
    void shouldThrowExceptionWhenTaskIsAlreadyCompleted() {
        // Setup
        Task existingTask = new Task();
        existingTask.setStatus(TaskStatus.COMPLETED); //

        TaskRequestDTO dto = new TaskRequestDTO("New Title", null, null, null, null);

        // Action & Assertions
        Assertions.assertThatThrownBy(() -> taskValidator.validateUpdate(existingTask, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("You cannot edit a task that has already been completed."); //
    }

    @Test
    @DisplayName("Cenário de Erro: Deve lançar exceção quando o novo título já pertence a OUTRA tarefa")
    void shouldThrowExceptionWhenTitleIsDuplicatedOnUpdate() {
        // Setup
        Long taskId = 1L;
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setStatus(TaskStatus.PENDING);

        String duplicatedTitle = "Title from another task";
        TaskRequestDTO dto = new TaskRequestDTO(duplicatedTitle, null, null, null, null);

        // Simula que encontramos uma tarefa diferente (ID 2L) com o mesmo título
        Task otherTask = new Task();
        otherTask.setId(2L);
        given(taskRepository.findByTitleIgnoreCase(duplicatedTitle)).willReturn(Optional.of(otherTask)); //

        // Action & Assertions
        Assertions.assertThatThrownBy(() -> taskValidator.validateUpdate(existingTask, dto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("There is already a task registered with this title."); //
    }

    @Test
    @DisplayName("Caminho Feliz: Deve permitir atualização quando o título é o mesmo da própria tarefa")
    void shouldPassValidationWhenUpdatingToSameTitle() {
        // Setup
        Long taskId = 1L;
        String title = "Same Title";

        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setTitle(title);
        existingTask.setStatus(TaskStatus.IN_PROGRESS);

        TaskRequestDTO dto = new TaskRequestDTO(title, null, null, null, null);

        // Simula que a busca pelo título retorna a própria tarefa que está sendo editada
        given(taskRepository.findByTitleIgnoreCase(title)).willReturn(Optional.of(existingTask)); //

        // Action & Assertions
        Assertions.assertThatCode(() -> taskValidator.validateUpdate(existingTask, dto))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Caminho Feliz: Deve permitir atualização quando o novo título é único no sistema")
    void shouldPassValidationWhenUpdatingToNewUniqueTitle() {
        // Setup
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setStatus(TaskStatus.PENDING);

        String newTitle = "Brand New Title";
        TaskRequestDTO dto = new TaskRequestDTO(newTitle, null, null, null, null);

        // Simula que não existe nenhuma tarefa com esse título no banco
        given(taskRepository.findByTitleIgnoreCase(newTitle)).willReturn(Optional.empty()); //

        // Action & Assertions
        Assertions.assertThatCode(() -> taskValidator.validateUpdate(existingTask, dto))
                .doesNotThrowAnyException();

        then(taskRepository).should(times(1)).findByTitleIgnoreCase(newTitle);
    }
}