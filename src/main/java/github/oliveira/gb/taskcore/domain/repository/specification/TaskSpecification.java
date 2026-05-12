package github.oliveira.gb.taskcore.domain.repository.specification;

import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskPriority;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

@UtilityClass
public class TaskSpecification {

    public static Specification<Task> hasText(String text) {
        return (root, query, criteriaBuilder) -> {
            String searchPattern = "%" + text.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
            );
        };
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasTags(Set<String> tags) {
        return (root, query, criteriaBuilder) -> {
            if (tags == null || tags.isEmpty()) {
                return null;
            }

            query.distinct(true);

            Join<Task, Tag> tagJoin = root.join("tags");
            return tagJoin.get("name").in(tags);
        };
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, criteriaBuilder) ->
                priority == null ? null : criteriaBuilder.equal(root.get("priority"), priority);
    }

    public static Specification<Task> isArchived(Boolean includeArchived) {
        return (root, query, criteriaBuilder) -> {
            if (includeArchived == null || !includeArchived) {
                return criteriaBuilder.equal(root.get("archived"), false);
            }
            return null;
        };
    }
}