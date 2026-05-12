package github.oliveira.gb.taskcore.domain.repository.specification;

import github.oliveira.gb.taskcore.domain.model.DeadlineFilter;
import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskPriority;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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

    public static Specification<Task> hasDeadlineFilter(Clock clock, DeadlineFilter deadlineFilter) {
        return (root, query, criteriaBuilder) -> {
            if (deadlineFilter == null) {
                return null;
            }

            LocalDateTime now = LocalDateTime.now(clock);

            return switch (deadlineFilter) {
                case OVERDUE -> createOverduePredicate(root, criteriaBuilder, now);
                case TODAY -> createTodayPredicate(root, criteriaBuilder, now);
                case THIS_WEEK -> createThisWeekPredicate(root, criteriaBuilder, now);
            };
        };
    }

    private static Predicate createOverduePredicate(
            jakarta.persistence.criteria.Root<Task> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            LocalDateTime now) {

        List<Predicate> predicates = new ArrayList<>();

        // dueDate < now
        predicates.add(criteriaBuilder.lessThan(root.get("dueDate"), now));

        // status != COMPLETED (excluir tarefas completadas)
        predicates.add(criteriaBuilder.notEqual(root.get("status"), TaskStatus.COMPLETED));

        // dueDate is not null
        predicates.add(criteriaBuilder.isNotNull(root.get("dueDate")));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private static Predicate createTodayPredicate(
            jakarta.persistence.criteria.Root<Task> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            LocalDateTime now) {

        LocalDate today = now.toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Predicate> predicates = new ArrayList<>();

        // dueDate between startOfDay and endOfDay
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startOfDay));
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endOfDay));

        // dueDate is not null
        predicates.add(criteriaBuilder.isNotNull(root.get("dueDate")));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private static Predicate createThisWeekPredicate(
            jakarta.persistence.criteria.Root<Task> root,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
            LocalDateTime now) {

        LocalDate today = now.toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfWeek = today.plusDays(6).atTime(LocalTime.MAX);

        List<Predicate> predicates = new ArrayList<>();

        // dueDate between today (00:00:00) and next 7 days (23:59:59)
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), startOfDay));
        predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), endOfWeek));

        // dueDate is not null
        predicates.add(criteriaBuilder.isNotNull(root.get("dueDate")));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
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