package github.oliveira.gb.taskcore.domain.repository;

import github.oliveira.gb.taskcore.domain.model.Subtask;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.specification.TaskSpecification;
import github.oliveira.gb.taskcore.infrastructure.config.JpaAuditConfig;
import org.assertj.core.api.Assertions;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditConfig.class)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve buscar Task por ID e carregar relacionamentos via EntityGraph")
    void shouldFindByIdWithEntityGraph() {
        Tag tag = new Tag();
        tag.setName("backend");
        entityManager.persist(tag);

        Task task = new Task();
        task.setTitle("Setup Database");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now().plusDays(1));

        Subtask subtask = new Subtask();
        subtask.setTitle("Create Flyway scripts");
        task.addSubtask(subtask);
        task.addTag(tag);

        Task savedTask = entityManager.persistFlushFind(task);
        entityManager.clear();

        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

        Assertions.assertThat(foundTask).isPresent();
        Assertions.assertThat(Hibernate.isInitialized(foundTask.get().getSubtasks())).isTrue();
        Assertions.assertThat(Hibernate.isInitialized(foundTask.get().getTags())).isTrue();
        Assertions.assertThat(foundTask.get().getSubtasks()).hasSize(1);
        Assertions.assertThat(foundTask.get().getTags()).hasSize(1);
    }

    @Test
    @DisplayName("Deve buscar Task pelo titulo ignorando case e carregar relacionamentos")
    void shouldFindByTitleIgnoreCaseWithEntityGraph() {
        Task task = new Task();
        task.setTitle("Study Spring Boot 3");
        task.setStatus(TaskStatus.IN_PROGRESS);
        entityManager.persistAndFlush(task);
        entityManager.clear();

        Optional<Task> foundTask = taskRepository.findByTitleIgnoreCase("sTuDy sPrInG bOoT 3");

        Assertions.assertThat(foundTask).isPresent();
        Assertions.assertThat(foundTask.get().getTitle()).isEqualTo("Study Spring Boot 3");
    }

    @Test
    @DisplayName("Deve retornar vazio quando o titulo nao existir")
    void shouldReturnEmptyWhenTitleDoesNotExist() {
        Optional<Task> foundTask = taskRepository.findByTitleIgnoreCase("Non Existent Task");

        Assertions.assertThat(foundTask).isEmpty();
    }

    @Test
    @DisplayName("Deve filtrar tarefas utilizando Specifications combinadas")
    void shouldFilterTasksUsingSpecifications() {
        Tag javaTag = new Tag();
        javaTag.setName("java");
        entityManager.persist(javaTag);

        Tag reactTag = new Tag();
        reactTag.setName("react");
        entityManager.persist(reactTag);

        Task task1 = new Task();
        task1.setTitle("Learn Java 21");
        task1.setDescription("Study virtual threads");
        task1.setStatus(TaskStatus.PENDING);
        task1.addTag(javaTag);
        entityManager.persist(task1);

        Task task2 = new Task();
        task2.setTitle("Learn React");
        task2.setDescription("Study hooks");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.addTag(reactTag);
        entityManager.persist(task2);

        Task task3 = new Task();
        task3.setTitle("Build API");
        task3.setDescription("Use Java and Spring");
        task3.setStatus(TaskStatus.PENDING);
        task3.addTag(javaTag);
        entityManager.persist(task3);

        entityManager.flush();
        entityManager.clear();

        Specification<Task> specByStatus = TaskSpecification.hasStatus(TaskStatus.PENDING);
        List<Task> pendingTasks = taskRepository.findAll(specByStatus);
        Assertions.assertThat(pendingTasks).hasSize(2).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Learn Java 21", "Build API");

        Specification<Task> specByText = TaskSpecification.hasText("study");
        List<Task> studyTasks = taskRepository.findAll(specByText);
        Assertions.assertThat(studyTasks).hasSize(2).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Learn Java 21", "Learn React");

        Specification<Task> specByTags = TaskSpecification.hasTags(Set.of("java"));
        List<Task> javaTasks = taskRepository.findAll(specByTags);
        Assertions.assertThat(javaTasks).hasSize(2).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Learn Java 21", "Build API");

        Specification<Task> combinedSpec = Specification.where(TaskSpecification.hasStatus(TaskStatus.PENDING))
                .and(TaskSpecification.hasText("virtual threads"));
        List<Task> combinedTasks = taskRepository.findAll(combinedSpec);
        Assertions.assertThat(combinedTasks).hasSize(1);
        Assertions.assertThat(combinedTasks.get(0).getTitle()).isEqualTo("Learn Java 21");
    }
}