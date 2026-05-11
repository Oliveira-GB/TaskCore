package github.oliveira.gb.taskcore.integration.repository;

import github.oliveira.gb.taskcore.domain.model.Subtask;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskPriority;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import github.oliveira.gb.taskcore.domain.repository.TagRepository;
import github.oliveira.gb.taskcore.domain.repository.specification.TaskSpecification;
import github.oliveira.gb.taskcore.integration.IntegrationTestBase;
import org.assertj.core.api.Assertions;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
class TaskRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TagRepository tagRepository;

    private Tag javaTag;
    private Tag reactTag;

    @BeforeEach
    void setUp() {
        javaTag = new Tag();
        javaTag.setName("java");
        javaTag = tagRepository.save(javaTag);

        reactTag = new Tag();
        reactTag.setName("react");
        reactTag = tagRepository.save(reactTag);
    }

    @Test
    @DisplayName("Should find task by ID and load relationships via EntityGraph")
    void shouldFindByIdWithEntityGraph() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now().plusDays(1));

        Subtask subtask = new Subtask();
        subtask.setTitle("Create subtask");
        task.addSubtask(subtask);
        task.addTag(javaTag);

        Task savedTask = taskRepository.save(task);

        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

        Assertions.assertThat(foundTask).isPresent();
        Assertions.assertThat(Hibernate.isInitialized(foundTask.get().getSubtasks())).isTrue();
        Assertions.assertThat(Hibernate.isInitialized(foundTask.get().getTags())).isTrue();
        Assertions.assertThat(foundTask.get().getSubtasks()).hasSize(1);
        Assertions.assertThat(foundTask.get().getTags()).hasSize(1);
    }

    @Test
    @DisplayName("Should find task by title ignoring case")
    void shouldFindByTitleIgnoreCase() {
        Task task = new Task();
        task.setTitle("Study Spring Boot 3");
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        Optional<Task> foundTask = taskRepository.findByTitleIgnoreCase("sTuDy sPrInG bOoT 3");

        Assertions.assertThat(foundTask).isPresent();
        Assertions.assertThat(foundTask.get().getTitle()).isEqualTo("Study Spring Boot 3");
    }

    @Test
    @DisplayName("Should return empty when title does not exist")
    void shouldReturnEmptyWhenTitleDoesNotExist() {
        Optional<Task> foundTask = taskRepository.findByTitleIgnoreCase("Non Existent Task");

        Assertions.assertThat(foundTask).isEmpty();
    }

    @Test
    @DisplayName("Should filter tasks by PENDING status using Specifications")
    void shouldFilterTasksByStatus() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatus.PENDING);
        task1.addTag(javaTag);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.addTag(reactTag);
        taskRepository.save(task2);

        Specification<Task> spec = TaskSpecification.hasStatus(TaskStatus.PENDING);
        List<Task> pendingTasks = taskRepository.findAll(spec);

        Assertions.assertThat(pendingTasks).hasSize(1);
        Assertions.assertThat(pendingTasks.get(0).getTitle()).isEqualTo("Task 1");
    }

    @Test
    @DisplayName("Should filter tasks by text search using Specifications")
    void shouldFilterTasksByText() {
        Task task1 = new Task();
        task1.setTitle("Learn Java 21");
        task1.setDescription("Study virtual threads");
        task1.setStatus(TaskStatus.PENDING);
        task1.addTag(javaTag);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Learn React");
        task2.setDescription("Study hooks");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.addTag(reactTag);
        taskRepository.save(task2);

        Specification<Task> spec = TaskSpecification.hasText("java");
        List<Task> javaTasks = taskRepository.findAll(spec);

        Assertions.assertThat(javaTasks).hasSize(1);
        Assertions.assertThat(javaTasks.get(0).getTitle()).isEqualTo("Learn Java 21");
    }

    @Test
    @DisplayName("Should filter tasks by tags using Specifications")
    void shouldFilterTasksByTags() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setStatus(TaskStatus.PENDING);
        task1.addTag(javaTag);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setStatus(TaskStatus.PENDING);
        task2.addTag(reactTag);
        taskRepository.save(task2);

        Task task3 = new Task();
        task3.setTitle("Task 3");
        task3.setStatus(TaskStatus.PENDING);
        task3.addTag(javaTag);
        task3.addTag(reactTag);
        taskRepository.save(task3);

        Specification<Task> spec = TaskSpecification.hasTags(Set.of("java"));
        List<Task> javaTasks = taskRepository.findAll(spec);

        Assertions.assertThat(javaTasks).hasSize(2);
    }

    @Test
    @DisplayName("Should filter tasks using combined Specifications")
    void shouldFilterTasksUsingCombinedSpecifications() {
        Task task1 = new Task();
        task1.setTitle("Learn Java 21");
        task1.setDescription("Study virtual threads");
        task1.setStatus(TaskStatus.PENDING);
        task1.addTag(javaTag);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Learn React");
        task2.setDescription("Study hooks");
        task2.setStatus(TaskStatus.IN_PROGRESS);
        task2.addTag(reactTag);
        taskRepository.save(task2);

        Task task3 = new Task();
        task3.setTitle("Build API");
        task3.setDescription("Use Java and Spring");
        task3.setStatus(TaskStatus.PENDING);
        task3.addTag(javaTag);
        taskRepository.save(task3);

        Specification<Task> combinedSpec = Specification.where(TaskSpecification.hasStatus(TaskStatus.PENDING))
                .and(TaskSpecification.hasText("virtual threads"));
        List<Task> combinedTasks = taskRepository.findAll(combinedSpec);

        Assertions.assertThat(combinedTasks).hasSize(1);
        Assertions.assertThat(combinedTasks.get(0).getTitle()).isEqualTo("Learn Java 21");
    }

    @Test
    @DisplayName("Should verify soft delete works correctly")
    void shouldSoftDeleteTask() {
        Task task = new Task();
        task.setTitle("To Delete");
        task.setStatus(TaskStatus.PENDING);
        Task savedTask = taskRepository.save(task);

        taskRepository.delete(savedTask);

        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());
        Assertions.assertThat(foundTask).isEmpty();
    }

    @Test
    @DisplayName("Should verify audit fields are populated")
    void shouldPopulateAuditFields() {
        Task task = new Task();
        task.setTitle("Audit Test");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.now().plusDays(1));
        Task savedTask = taskRepository.save(task);

        Assertions.assertThat(savedTask.getCreatedAt()).isNotNull();
        Assertions.assertThat(savedTask.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should save and retrieve tasks with different priorities")
    void shouldSaveAndRetrieveTasksWithDifferentPriorities() {
        Task criticalTask = new Task();
        criticalTask.setTitle("Critical Task");
        criticalTask.setStatus(TaskStatus.PENDING);
        criticalTask.setPriority(TaskPriority.CRITICAL);
        taskRepository.save(criticalTask);

        Task highTask = new Task();
        highTask.setTitle("High Task");
        highTask.setStatus(TaskStatus.PENDING);
        highTask.setPriority(TaskPriority.HIGH);
        taskRepository.save(highTask);

        Task mediumTask = new Task();
        mediumTask.setTitle("Medium Task");
        mediumTask.setStatus(TaskStatus.PENDING);
        mediumTask.setPriority(TaskPriority.MEDIUM);
        taskRepository.save(mediumTask);

        Task lowTask = new Task();
        lowTask.setTitle("Low Task");
        lowTask.setStatus(TaskStatus.PENDING);
        lowTask.setPriority(TaskPriority.LOW);
        taskRepository.save(lowTask);

        List<Task> allTasks = taskRepository.findAll();

        Assertions.assertThat(allTasks)
                .extracting(Task::getPriority)
                .containsExactlyInAnyOrder(
                        TaskPriority.CRITICAL,
                        TaskPriority.HIGH,
                        TaskPriority.MEDIUM,
                        TaskPriority.LOW
                );
    }

    @Test
    @DisplayName("Should filter tasks by priority using Specifications")
    void shouldFilterTasksByPriority() {
        Task highTask = new Task();
        highTask.setTitle("High Priority Task");
        highTask.setStatus(TaskStatus.PENDING);
        highTask.setPriority(TaskPriority.HIGH);
        taskRepository.save(highTask);

        Task mediumTask = new Task();
        mediumTask.setTitle("Medium Priority Task");
        mediumTask.setStatus(TaskStatus.PENDING);
        mediumTask.setPriority(TaskPriority.MEDIUM);
        taskRepository.save(mediumTask);

        Specification<Task> spec = TaskSpecification.hasPriority(TaskPriority.HIGH);
        List<Task> highPriorityTasks = taskRepository.findAll(spec);

        Assertions.assertThat(highPriorityTasks).hasSize(1);
        Assertions.assertThat(highPriorityTasks.get(0).getTitle()).isEqualTo("High Priority Task");
        Assertions.assertThat(highPriorityTasks.get(0).getPriority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    @DisplayName("Should default to MEDIUM priority when not specified")
    void shouldDefaultToMediumPriority() {
        Task task = new Task();
        task.setTitle("Default Priority Task");
        task.setStatus(TaskStatus.PENDING);

        Task savedTask = taskRepository.save(task);

        Assertions.assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
    }
}