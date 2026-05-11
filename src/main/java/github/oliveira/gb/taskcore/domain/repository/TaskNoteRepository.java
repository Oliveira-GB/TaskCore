package github.oliveira.gb.taskcore.domain.repository;

import github.oliveira.gb.taskcore.domain.model.TaskNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for TaskNote entities.
 * Supports soft delete via @SQLDelete and @SQLRestriction on the entity.
 */
@Repository
public interface TaskNoteRepository extends JpaRepository<TaskNote, Long> {
    // Standard CRUD methods are inherited
    // Soft delete is handled automatically by entity annotations
}
