package github.oliveira.gb.taskcore.domain.model;

import github.oliveira.gb.taskcore.infrastructure.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity representing a note attached to a task.
 * Supports soft delete via @SQLDelete and @SQLRestriction.
 */
@Entity
@Table(name = "task_notes")
@SQLDelete(sql = "UPDATE task_notes SET active = false WHERE id = ?")
@SQLRestriction("active = true")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
public class TaskNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    @NotNull
    private Task task;

    @Column(name = "content", nullable = false, length = 1000)
    @NotBlank(message = "Note content cannot be blank")
    @Size(max = 1000, message = "Note content must not exceed 1000 characters")
    private String content;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
