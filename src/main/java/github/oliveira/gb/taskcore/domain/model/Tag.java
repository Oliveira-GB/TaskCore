package github.oliveira.gb.taskcore.domain.model;

import github.oliveira.gb.taskcore.infrastructure.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLDelete(sql = "UPDATE tags SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    @Size(min = 2, max = 20)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    @PreUpdate
    private void normalizeName() {
        if (this.name != null) {
            this.name = this.name.toLowerCase().trim();
        }
    }
}