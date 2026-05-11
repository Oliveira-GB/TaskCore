package github.oliveira.gb.taskcore.domain.repository;

import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.infrastructure.config.JpaAuditConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditConfig.class)
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("Caminho Feliz: Deve encontrar a tag pelo nome")
    void shouldFindTagByNameWhenItExists() {
        Tag tag = new Tag();
        tag.setName("java");
        tagRepository.save(tag);

        Optional<Tag> foundTag = tagRepository.findByName("java");

        Assertions.assertThat(foundTag).isPresent();
        Assertions.assertThat(foundTag.get().getName()).isEqualTo("java");
        Assertions.assertThat(foundTag.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("Cenário de Erro: Deve retornar Optional vazio quando nome nao existir")
    void shouldReturnEmptyOptionalWhenTagDoesNotExist() {
        Optional<Tag> foundTag = tagRepository.findByName("golang");

        Assertions.assertThat(foundTag).isEmpty();
    }
}