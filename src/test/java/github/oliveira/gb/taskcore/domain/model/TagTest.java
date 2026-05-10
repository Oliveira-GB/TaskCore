package github.oliveira.gb.taskcore.domain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TagTest {

    @Test
    @DisplayName("Should trim and lowercase the tag name during normalization")
    void shouldNormalizeName() {
        Tag tag = new Tag();
        tag.setName("   SpRinG bOoT   ");
        
        ReflectionTestUtils.invokeMethod(tag, "normalizeName");

        Assertions.assertThat(tag.getName()).isEqualTo("spring boot");
    }

    @Test
    @DisplayName("Should handle null name safely during normalization")
    void shouldHandleNullNameSafely() {
        Tag tag = new Tag();
        tag.setName(null);
        
        ReflectionTestUtils.invokeMethod(tag, "normalizeName");
        
        Assertions.assertThat(tag.getName()).isNull();
    }
}