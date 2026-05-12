package github.oliveira.gb.taskcore.domain.repository.specification;

import github.oliveira.gb.taskcore.domain.model.DeadlineFilter;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskPriority;
import github.oliveira.gb.taskcore.domain.model.TaskStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TaskSpecificationTest {

    @Mock
    private Root<Task> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Test
    @DisplayName("hasText: Deve criar predicate OR com lower e wildcards")
    @SuppressWarnings("unchecked")
    void shouldCreateHasTextSpecification() {
        String searchText = "Spring";
        String expectedPattern = "%spring%";

        Path<String> titlePath = mock(Path.class);
        Path<String> descriptionPath = mock(Path.class);
        Expression<String> lowerTitleExpr = mock(Expression.class);
        Expression<String> lowerDescExpr = mock(Expression.class);
        Predicate titlePredicate = mock(Predicate.class);
        Predicate descPredicate = mock(Predicate.class);
        Predicate orPredicate = mock(Predicate.class);

        given(root.<String>get("title")).willReturn(titlePath);
        given(root.<String>get("description")).willReturn(descriptionPath);

        given(criteriaBuilder.lower(titlePath)).willReturn(lowerTitleExpr);
        given(criteriaBuilder.lower(descriptionPath)).willReturn(lowerDescExpr);

        given(criteriaBuilder.like(lowerTitleExpr, expectedPattern)).willReturn(titlePredicate);
        given(criteriaBuilder.like(lowerDescExpr, expectedPattern)).willReturn(descPredicate);

        given(criteriaBuilder.or(titlePredicate, descPredicate)).willReturn(orPredicate);

        Specification<Task> spec = TaskSpecification.hasText(searchText);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(orPredicate);
    }

    @Test
    @DisplayName("hasStatus: Deve criar predicate de igualdade para o status")
    @SuppressWarnings("unchecked")
    void shouldCreateHasStatusSpecification() {
        TaskStatus status = TaskStatus.PENDING;
        Path<Object> statusPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        given(root.get("status")).willReturn(statusPath);
        given(criteriaBuilder.equal(statusPath, status)).willReturn(equalPredicate);

        Specification<Task> spec = TaskSpecification.hasStatus(status);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(equalPredicate);
    }

    @Test
    @DisplayName("hasTags: Deve retornar null se o set de tags for nulo ou vazio")
    void shouldReturnNullWhenTagsAreNullOrEmpty() {
        Specification<Task> specNull = TaskSpecification.hasTags(null);
        Predicate resultNull = specNull.toPredicate(root, query, criteriaBuilder);
        Assertions.assertThat(resultNull).isNull();

        Specification<Task> specEmpty = TaskSpecification.hasTags(java.util.Collections.emptySet());
        Predicate resultEmpty = specEmpty.toPredicate(root, query, criteriaBuilder);
        Assertions.assertThat(resultEmpty).isNull();
    }

    @Test
    @DisplayName("hasTags: Deve criar predicate com distinct, join e in clause")
    @SuppressWarnings("unchecked")
    void shouldCreateHasTagsSpecification() {
        java.util.Set<String> tags = java.util.Set.of("java", "spring");

        jakarta.persistence.criteria.Join<Task, github.oliveira.gb.taskcore.domain.model.Tag> tagJoin = mock(jakarta.persistence.criteria.Join.class);
        jakarta.persistence.criteria.Path<Object> namePath = mock(jakarta.persistence.criteria.Path.class);
        jakarta.persistence.criteria.CriteriaBuilder.In<Object> inClause = mock(jakarta.persistence.criteria.CriteriaBuilder.In.class);

        given(root.<Task, github.oliveira.gb.taskcore.domain.model.Tag>join("tags")).willReturn(tagJoin);
        given(tagJoin.get("name")).willReturn(namePath);
        given(namePath.in(tags)).willReturn(inClause);

        Specification<Task> spec = TaskSpecification.hasTags(tags);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        org.mockito.BDDMockito.then(query).should().distinct(true);
        Assertions.assertThat(result).isEqualTo(inClause);
    }

    @Test
    @DisplayName("hasPriority: Deve retornar null quando priority for nulo")
    void shouldReturnNullWhenPriorityIsNull() {
        Specification<Task> spec = TaskSpecification.hasPriority(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        Assertions.assertThat(result).isNull();
    }

    @Test
    @DisplayName("hasPriority: Deve criar predicate de igualdade para a prioridade")
    @SuppressWarnings("unchecked")
    void shouldCreateHasPrioritySpecification() {
        TaskPriority priority = TaskPriority.HIGH;
        Path<Object> priorityPath = mock(Path.class);
        Predicate equalPredicate = mock(Predicate.class);

        given(root.get("priority")).willReturn(priorityPath);
        given(criteriaBuilder.equal(priorityPath, priority)).willReturn(equalPredicate);

        Specification<Task> spec = TaskSpecification.hasPriority(priority);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(equalPredicate);
    }

    @Test
    @DisplayName("hasDeadlineFilter: Deve retornar null quando deadlineFilter for nulo")
    void shouldReturnNullWhenDeadlineFilterIsNull() {
        Clock fixedClock = Clock.fixed(Instant.parse("2024-06-15T12:00:00Z"), ZoneId.of("UTC"));

        Specification<Task> spec = TaskSpecification.hasDeadlineFilter(fixedClock, null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isNull();
    }

    @Test
    @DisplayName("hasDeadlineFilter OVERDUE: Deve criar predicates corretos")
    @SuppressWarnings("unchecked")
    void shouldCreateOverduePredicate() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 6, 15, 12, 0);
        Clock fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

        Path<LocalDateTime> dueDatePath = mock(Path.class);
        Path<TaskStatus> statusPath = mock(Path.class);
        Predicate lessThanPredicate = mock(Predicate.class);
        Predicate notEqualPredicate = mock(Predicate.class);
        Predicate isNotNullPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        given(root.<LocalDateTime>get("dueDate")).willReturn(dueDatePath);
        given(root.<TaskStatus>get("status")).willReturn(statusPath);
        given(criteriaBuilder.lessThan(dueDatePath, fixedNow)).willReturn(lessThanPredicate);
        given(criteriaBuilder.notEqual(statusPath, TaskStatus.COMPLETED)).willReturn(notEqualPredicate);
        given(criteriaBuilder.isNotNull(dueDatePath)).willReturn(isNotNullPredicate);
        given(criteriaBuilder.and(lessThanPredicate, notEqualPredicate, isNotNullPredicate)).willReturn(andPredicate);

        Specification<Task> spec = TaskSpecification.hasDeadlineFilter(fixedClock, DeadlineFilter.OVERDUE);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(andPredicate);
    }

    @Test
    @DisplayName("hasDeadlineFilter TODAY: Deve criar predicates corretos")
    @SuppressWarnings("unchecked")
    void shouldCreateTodayPredicate() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 6, 15, 12, 0);
        Clock fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

        Path<LocalDateTime> dueDatePath = mock(Path.class);
        Predicate greaterThanOrEqualPredicate = mock(Predicate.class);
        Predicate lessThanOrEqualPredicate = mock(Predicate.class);
        Predicate isNotNullPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        given(root.<LocalDateTime>get("dueDate")).willReturn(dueDatePath);
        given(criteriaBuilder.greaterThanOrEqualTo(dueDatePath, fixedNow.toLocalDate().atStartOfDay())).willReturn(greaterThanOrEqualPredicate);
        given(criteriaBuilder.lessThanOrEqualTo(dueDatePath, fixedNow.toLocalDate().atTime(java.time.LocalTime.MAX))).willReturn(lessThanOrEqualPredicate);
        given(criteriaBuilder.isNotNull(dueDatePath)).willReturn(isNotNullPredicate);
        given(criteriaBuilder.and(greaterThanOrEqualPredicate, lessThanOrEqualPredicate, isNotNullPredicate)).willReturn(andPredicate);

        Specification<Task> spec = TaskSpecification.hasDeadlineFilter(fixedClock, DeadlineFilter.TODAY);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(andPredicate);
    }

    @Test
    @DisplayName("hasDeadlineFilter THIS_WEEK: Deve criar predicates corretos")
    @SuppressWarnings("unchecked")
    void shouldCreateThisWeekPredicate() {
        LocalDateTime fixedNow = LocalDateTime.of(2024, 6, 15, 12, 0);
        Clock fixedClock = Clock.fixed(fixedNow.atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

        Path<LocalDateTime> dueDatePath = mock(Path.class);
        Predicate greaterThanOrEqualPredicate = mock(Predicate.class);
        Predicate lessThanOrEqualPredicate = mock(Predicate.class);
        Predicate isNotNullPredicate = mock(Predicate.class);
        Predicate andPredicate = mock(Predicate.class);

        given(root.<LocalDateTime>get("dueDate")).willReturn(dueDatePath);
        given(criteriaBuilder.greaterThanOrEqualTo(dueDatePath, fixedNow.toLocalDate().atStartOfDay())).willReturn(greaterThanOrEqualPredicate);
        given(criteriaBuilder.lessThanOrEqualTo(dueDatePath, fixedNow.toLocalDate().plusDays(6).atTime(java.time.LocalTime.MAX))).willReturn(lessThanOrEqualPredicate);
        given(criteriaBuilder.isNotNull(dueDatePath)).willReturn(isNotNullPredicate);
        given(criteriaBuilder.and(greaterThanOrEqualPredicate, lessThanOrEqualPredicate, isNotNullPredicate)).willReturn(andPredicate);

        Specification<Task> spec = TaskSpecification.hasDeadlineFilter(fixedClock, DeadlineFilter.THIS_WEEK);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(andPredicate);
    }

    @Test
    @DisplayName("isArchived: Deve criar predicate archived = false quando includeArchived for false")
    @SuppressWarnings("unchecked")
    void shouldCreateIsArchivedFalseSpecification() {
        Path<Object> archivedPath = mock(Path.class);
        Predicate falsePredicate = mock(Predicate.class);

        given(root.get("archived")).willReturn(archivedPath);
        given(criteriaBuilder.equal(archivedPath, false)).willReturn(falsePredicate);

        Specification<Task> spec = TaskSpecification.isArchived(false);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(falsePredicate);
    }

    @Test
    @DisplayName("isArchived: Deve retornar null quando includeArchived for true")
    void shouldReturnNullWhenIncludeArchivedIsTrue() {
        Specification<Task> spec = TaskSpecification.isArchived(true);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);
        Assertions.assertThat(result).isNull();
    }

    @Test
    @DisplayName("isArchived: Deve criar predicate archived = false quando includeArchived for null")
    @SuppressWarnings("unchecked")
    void shouldCreateIsArchivedFalseWhenIncludeArchivedIsNull() {
        Path<Object> archivedPath = mock(Path.class);
        Predicate falsePredicate = mock(Predicate.class);

        given(root.get("archived")).willReturn(archivedPath);
        given(criteriaBuilder.equal(archivedPath, false)).willReturn(falsePredicate);

        Specification<Task> spec = TaskSpecification.isArchived(null);
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        Assertions.assertThat(result).isEqualTo(falsePredicate);
    }
}