package github.oliveira.gb.taskcore.domain.model;

import github.oliveira.gb.taskcore.infrastructure.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;



@Entity
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
public class Task extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    @NotBlank(message = "It is not possible to create a task without a TITLE")
    @Size(max = 100)
    private String title;

    @Column(name = "description", length = 500)
    @Size(max = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private TaskStatus status;

    @Column(name = "due_date")
    @Future(message = "Every date must have a due date in the future!")
    private LocalDateTime dueDate;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
