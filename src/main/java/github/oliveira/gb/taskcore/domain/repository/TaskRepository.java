package github.oliveira.gb.taskcore.domain.repository;

import github.oliveira.gb.taskcore.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
