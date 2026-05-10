package github.oliveira.gb.taskcore.integration.repository;

import github.oliveira.gb.taskcore.domain.model.Tag;
import github.oliveira.gb.taskcore.domain.repository.TagRepository;
import github.oliveira.gb.taskcore.integration.IntegrationTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TagRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("Should save tag and normalize name to lowercase")
    void shouldSaveTagAndNormalizeName() {
        Tag tag = new Tag();
        tag.setName("JAVA");
        Tag savedTag = tagRepository.save(tag);

        Assertions.assertThat(savedTag.getId()).isNotNull();
        Assertions.assertThat(savedTag.getName()).isEqualTo("java");
    }

    @Test
    @DisplayName("Should find tag by name when it exists")
    void shouldFindTagByNameWhenItExists() {
        Tag tag = new Tag();
        tag.setName("backend");
        tagRepository.save(tag);

        Optional<Tag> foundTag = tagRepository.findByName("backend");

        Assertions.assertThat(foundTag).isPresent();
        Assertions.assertThat(foundTag.get().getName()).isEqualTo("backend");
    }

    @Test
    @DisplayName("Should return empty Optional when tag does not exist")
    void shouldReturnEmptyOptionalWhenTagDoesNotExist() {
        Optional<Tag> foundTag = tagRepository.findByName("golang");

        Assertions.assertThat(foundTag).isEmpty();
    }

    @Test
    @DisplayName("Should normalize name on save (trim and lowercase)")
    void shouldNormalizeNameOnSave() {
        Tag tag = new Tag();
        tag.setName("  SPRING BOOT  ");
        Tag savedTag = tagRepository.save(tag);

        Assertions.assertThat(savedTag.getName()).isEqualTo("spring boot");
    }

    @Test
    @DisplayName("Should enforce unique constraint on name")
    void shouldEnforceUniqueConstraintOnName() {
        Tag tag1 = new Tag();
        tag1.setName("unique");
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("unique");

        Assertions.assertThatThrownBy(() -> tagRepository.save(tag2))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should soft delete tag successfully")
    void shouldSoftDeleteTag() {
        Tag tag = new Tag();
        tag.setName("to-delete");
        Tag savedTag = tagRepository.save(tag);

        tagRepository.delete(savedTag);

        Optional<Tag> foundTag = tagRepository.findById(savedTag.getId());
        Assertions.assertThat(foundTag).isEmpty();
    }

    @Test
    @DisplayName("Should return tag with active=true when active tag exists")
    void shouldReturnTagWithActiveTrue() {
        Tag tag = new Tag();
        tag.setName("active-tag");
        tag.setActive(true);
        Tag savedTag = tagRepository.save(tag);

        Optional<Tag> foundTag = tagRepository.findById(savedTag.getId());

        Assertions.assertThat(foundTag).isPresent();
        Assertions.assertThat(foundTag.get().isActive()).isTrue();
    }
}