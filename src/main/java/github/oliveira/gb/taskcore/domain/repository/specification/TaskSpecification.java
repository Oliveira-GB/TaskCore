package github.oliveira.gb.taskcore.domain.repository.specification;

import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

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
}