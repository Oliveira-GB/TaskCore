package github.oliveira.gb.taskcore.domain.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaskPriorityTest {

    @Test
    @DisplayName("Should have exactly four priority levels: LOW, MEDIUM, HIGH, CRITICAL")
    void shouldHaveFourPriorityLevels() {
        Assertions.assertThat(TaskPriority.values())
                .hasSize(4)
                .containsExactly(TaskPriority.CRITICAL, TaskPriority.HIGH, TaskPriority.MEDIUM, TaskPriority.LOW);
    }

    @Test
    @DisplayName("CRITICAL should have weight 1 (highest priority)")
    void criticalShouldHaveWeightOne() {
        Assertions.assertThat(TaskPriority.CRITICAL.getWeight()).isEqualTo(1);
    }

    @Test
    @DisplayName("HIGH should have weight 2")
    void highShouldHaveWeightTwo() {
        Assertions.assertThat(TaskPriority.HIGH.getWeight()).isEqualTo(2);
    }

    @Test
    @DisplayName("MEDIUM should have weight 3")
    void mediumShouldHaveWeightThree() {
        Assertions.assertThat(TaskPriority.MEDIUM.getWeight()).isEqualTo(3);
    }

    @Test
    @DisplayName("LOW should have weight 4 (lowest priority)")
    void lowShouldHaveWeightFour() {
        Assertions.assertThat(TaskPriority.LOW.getWeight()).isEqualTo(4);
    }

    @Test
    @DisplayName("Weight order should follow: CRITICAL < HIGH < MEDIUM < LOW")
    void weightOrderShouldFollowPriorityHierarchy() {
        Assertions.assertThat(TaskPriority.CRITICAL.getWeight())
                .isLessThan(TaskPriority.HIGH.getWeight())
                .isLessThan(TaskPriority.MEDIUM.getWeight())
                .isLessThan(TaskPriority.LOW.getWeight());
    }
}
