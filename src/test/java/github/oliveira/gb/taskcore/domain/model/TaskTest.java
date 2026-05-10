package github.oliveira.gb.taskcore.domain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    @DisplayName("Should add a subtask and maintain bidirectional relationship")
    void shouldAddSubtaskAndSetParentTask() {
        // Setup
        Task task = new Task();
        Subtask subtask = new Subtask();

        // Action
        task.addSubtask(subtask);

        // Assertions
        Assertions.assertThat(task.getSubtasks()).containsExactly(subtask);
        Assertions.assertThat(subtask.getTask()).isEqualTo(task);
    }

    @Test
    @DisplayName("Should remove a subtask and break bidirectional relationship")
    void shouldRemoveSubtaskAndClearParentTask() {
        // Setup
        Task task = new Task();
        Subtask subtask = new Subtask();
        task.addSubtask(subtask); // Adiciona primeiro

        // Action
        task.removeSubtask(subtask);

        // Assertions
        Assertions.assertThat(task.getSubtasks()).isEmpty();
        Assertions.assertThat(subtask.getTask()).isNull();
    }

    @Test
    @DisplayName("Should add and remove tags successfully")
    void shouldManageTags() {
        // Setup
        Task task = new Task();

        Tag tag1 = new Tag();
        tag1.setId(1L); // Definindo um ID único
        tag1.setName("java");

        Tag tag2 = new Tag();
        tag2.setId(2L); // Definindo um ID único
        tag2.setName("spring");

        // Action & Assertions - Adição
        task.addTag(tag1);
        task.addTag(tag2);
        Assertions.assertThat(task.getTags()).containsExactlyInAnyOrder(tag1, tag2);

        // Action & Assertions - Remoção
        task.removeTag(tag1);
        Assertions.assertThat(task.getTags()).containsExactly(tag2);
    }
}