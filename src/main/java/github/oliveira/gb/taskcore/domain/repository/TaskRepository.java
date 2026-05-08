package github.oliveira.gb.taskcore.domain.repository;

import github.oliveira.gb.taskcore.domain.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @EntityGraph(attributePaths = {"subtasks", "tags"})
    Optional<Task> findById(Long id);

    @EntityGraph(attributePaths = {"subtasks", "tags"})
    Optional<Task> findByTitleIgnoreCase(String title);
}