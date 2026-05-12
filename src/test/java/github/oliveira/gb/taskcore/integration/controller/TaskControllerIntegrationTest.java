package github.oliveira.gb.taskcore.integration.controller;

import github.oliveira.gb.taskcore.api.dto.request.TaskRequestDTO;
import github.oliveira.gb.taskcore.domain.model.Task;
import github.oliveira.gb.taskcore.domain.model.TaskPriority;
import github.oliveira.gb.taskcore.domain.repository.TaskRepository;
import github.oliveira.gb.taskcore.integration.IntegrationTestBase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(scripts = "/clean-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TaskControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    private TaskRequestDTO validRequest;
    private final String baseUrl = "/api/v1/tasks";

    @BeforeEach
    void setUp() {
        validRequest = new TaskRequestDTO(
                "Study Spring Boot",
                "Review Swagger annotations and apply to controller",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );
    }

    @Test
    @DisplayName("E2E: Should create task with valid payload and return 201 Created")
    void shouldCreateTaskSuccessfully() throws Exception {
        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Study Spring Boot"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for blank title")
    void shouldReturn400ForBlankTitle() throws Exception {
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "",
                "Description",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for past due date")
    void shouldReturn400ForPastDueDate() throws Exception {
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "Valid Title",
                "Description",
                LocalDateTime.now().minusDays(1),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for title too short")
    void shouldReturn400ForTitleTooShort() throws Exception {
        TaskRequestDTO invalidRequest = new TaskRequestDTO(
                "AB",
                "Description",
                LocalDateTime.now().plusDays(2),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should find task by ID and return 200 OK")
    void shouldFindTaskById() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO(
                "Find Me",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        mockMvc.perform(get(baseUrl + "/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Find Me"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 404 Not Found for non-existent task")
    void shouldReturn404ForNonExistentTask() throws Exception {
        mockMvc.perform(get(baseUrl + "/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.messages[0]").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should update task and return 200 OK")
    void shouldUpdateTaskSuccessfully() throws Exception {
        TaskRequestDTO createRequest = new TaskRequestDTO(
                "Original Title",
                "Original Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        TaskRequestDTO updateRequest = new TaskRequestDTO(
                "Updated Title",
                "Updated Description",
                LocalDateTime.now().plusDays(10),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(put(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 404 Not Found when updating non-existent task")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {
        TaskRequestDTO updateRequest = new TaskRequestDTO(
                "Updated Title",
                "Updated Description",
                LocalDateTime.now().plusDays(10),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(put(baseUrl + "/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should delete task and return 204 No Content")
    void shouldDeleteTaskSuccessfully() throws Exception {
        TaskRequestDTO createRequest = new TaskRequestDTO(
                "To Delete",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        mockMvc.perform(delete(baseUrl + "/{id}", taskId))
                .andExpect(status().isNoContent())
                .andDo(print());

        Optional<Task> deletedTask = taskRepository.findById(taskId);
        Assertions.assertThat(deletedTask).isEmpty();
    }

    @Test
    @DisplayName("E2E: Should return 404 Not Found when deleting non-existent task")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {
        mockMvc.perform(delete(baseUrl + "/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return paginated tasks with 200 OK")
    void shouldReturnPaginatedTasks() throws Exception {
        for (int i = 0; i < 3; i++) {
            TaskRequestDTO request = new TaskRequestDTO(
                    "Task " + i,
                    "Description " + i,
                    LocalDateTime.now().plusDays(i + 1),
                    Collections.emptyList(),
                    Collections.emptySet(),
                    null,
                    null
            );
            mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get(baseUrl)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should create task with specific priority and return priority in response")
    void shouldCreateTaskWithSpecificPriority() throws Exception {
        TaskRequestDTO requestWithPriority = new TaskRequestDTO(
                "High Priority Task",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                TaskPriority.HIGH,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(requestWithPriority)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should default to MEDIUM priority when priority is not provided")
    void shouldDefaultToMediumPriorityWhenNotProvided() throws Exception {
        String jsonWithoutPriority = """
                {
                    "title": "Task Without Priority",
                    "description": "Description",
                    "dueDate": "%s"
                }
                """.formatted(LocalDateTime.now().plusDays(5).toString());

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutPriority))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should update task priority successfully")
    void shouldUpdateTaskPriority() throws Exception {
        TaskRequestDTO createRequest = new TaskRequestDTO(
                "Task to Update Priority",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                TaskPriority.LOW,
                null
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        TaskRequestDTO updateRequest = new TaskRequestDTO(
                "Task to Update Priority",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                TaskPriority.CRITICAL,
                null
        );

        mockMvc.perform(put(baseUrl + "/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should filter tasks by priority query parameter")
    void shouldFilterTasksByPriority() throws Exception {
        TaskRequestDTO highPriorityRequest = new TaskRequestDTO(
                "High Priority Task",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                TaskPriority.HIGH,
                null
        );

        TaskRequestDTO mediumPriorityRequest = new TaskRequestDTO(
                "Medium Priority Task",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                TaskPriority.MEDIUM,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(highPriorityRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(mediumPriorityRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(baseUrl)
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].priority").value("HIGH"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for invalid priority value")
    void shouldReturn400ForInvalidPriority() throws Exception {
        String jsonWithInvalidPriority = """
                {
                    "title": "Task With Invalid Priority",
                    "description": "Description",
                    "dueDate": "%s",
                    "priority": "INVALID_PRIORITY"
                }
                """.formatted(LocalDateTime.now().plusDays(5).toString());

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithInvalidPriority))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should update task status to COMPLETED and cascade to subtasks")
    void shouldUpdateStatusToCompletedAndCascadeToSubtasks() throws Exception {
        // First create a task with subtasks
        String jsonWithSubtasks = """
                {
                    "title": "Task with Subtasks for Cascade Test",
                    "description": "Description",
                    "dueDate": "%s",
                    "subtasks": [
                        {"title": "Subtask 1", "completed": false},
                        {"title": "Subtask 2", "completed": false}
                    ]
                }
                """.formatted(LocalDateTime.now().plusDays(5).toString());

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithSubtasks))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        // Verify initial state - subtasks should not be completed
        mockMvc.perform(get(baseUrl + "/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.subtasks[0].completed").value(false))
                .andExpect(jsonPath("$.subtasks[1].completed").value(false))
                .andExpect(jsonPath("$.progress").value(0.00));

        // Update status to COMPLETED
        String statusUpdateJson = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(patch(baseUrl + "/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.subtasks[0].completed").value(true))
                .andExpect(jsonPath("$.subtasks[1].completed").value(true))
                .andExpect(jsonPath("$.progress").value(100.00))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should reopen task without modifying subtasks (Isolation Rule)")
    void shouldReopenTaskWithoutModifyingSubtasks() throws Exception {
        // Create a completed task with completed subtasks
        String jsonWithCompletedSubtasks = """
                {
                    "title": "Completed Task for Reopen Test",
                    "description": "Description",
                    "dueDate": "%s",
                    "subtasks": [
                        {"title": "Subtask 1", "completed": true},
                        {"title": "Subtask 2", "completed": true}
                    ]
                }
                """.formatted(LocalDateTime.now().plusDays(5).toString());

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithCompletedSubtasks))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        // Complete the task first
        String completeStatusJson = """
                {
                    "status": "COMPLETED"
                }
                """;

        mockMvc.perform(patch(baseUrl + "/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeStatusJson))
                .andExpect(status().isOk());

        // Reopen the task to PENDING
        String reopenStatusJson = """
                {
                    "status": "PENDING"
                }
                """;

        mockMvc.perform(patch(baseUrl + "/{id}/status", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reopenStatusJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                // Subtasks should remain completed (Isolation Rule)
                .andExpect(jsonPath("$.subtasks[0].completed").value(true))
                .andExpect(jsonPath("$.subtasks[1].completed").value(true))
                // Progress should remain 100% since subtasks are still completed
                .andExpect(jsonPath("$.progress").value(100.00))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return progress field in task detail response")
    void shouldReturnProgressFieldInDetailResponse() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO(
                "Task for Progress Test",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        MvcResult createResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long taskId = extractIdFromLocation(createResult.getResponse().getHeader("Location"));

        // Verify progress field exists in detail response
        mockMvc.perform(get(baseUrl + "/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").exists())
                .andExpect(jsonPath("$.progress").value(0.00))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should NOT return progress field in list response")
    void shouldNotReturnProgressFieldInListResponse() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO(
                "Task for List Progress Test",
                "Description",
                LocalDateTime.now().plusDays(5),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated());

        // Verify progress field does NOT exist in list response
        mockMvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].progress").doesNotExist())
                .andExpect(jsonPath("$.content[0].subtasks").doesNotExist())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request when status is null in PATCH")
    void shouldReturn400ForNullStatus() throws Exception {
        String jsonWithNullStatus = """
                {
                    "status": null
                }
                """;

        mockMvc.perform(patch(baseUrl + "/{id}/status", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNullStatus))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should filter tasks by deadline=TODAY and return 200 OK")
    void shouldFilterTasksByDeadlineToday() throws Exception {
        // Create a task due today
        TaskRequestDTO todayTask = new TaskRequestDTO(
                "Task Due Today",
                "Description",
                LocalDateTime.now(),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        // Create a task due tomorrow
        TaskRequestDTO tomorrowTask = new TaskRequestDTO(
                "Task Due Tomorrow",
                "Description",
                LocalDateTime.now().plusDays(1),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(todayTask)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(tomorrowTask)))
                .andExpect(status().isCreated());

        // Filter by TODAY
        mockMvc.perform(get(baseUrl)
                        .param("deadline", "TODAY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Task Due Today"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should filter tasks by deadline=THIS_WEEK and return 200 OK")
    void shouldFilterTasksByDeadlineThisWeek() throws Exception {
        // Create a task due in 3 days
        TaskRequestDTO thisWeekTask = new TaskRequestDTO(
                "Task Due This Week",
                "Description",
                LocalDateTime.now().plusDays(3),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        // Create a task due in 10 days (outside this week)
        TaskRequestDTO nextWeekTask = new TaskRequestDTO(
                "Task Due Next Week",
                "Description",
                LocalDateTime.now().plusDays(10),
                Collections.emptyList(),
                Collections.emptySet(),
                null,
                null
        );

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(thisWeekTask)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(nextWeekTask)))
                .andExpect(status().isCreated());

        // Filter by THIS_WEEK
        mockMvc.perform(get(baseUrl)
                        .param("deadline", "THIS_WEEK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Task Due This Week"))
                .andDo(print());
    }

    @Test
    @DisplayName("E2E: Should return 400 Bad Request for invalid deadline filter value")
    void shouldReturn400ForInvalidDeadlineFilter() throws Exception {
        mockMvc.perform(get(baseUrl)
                        .param("deadline", "INVALID_FILTER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages[0]").exists())
                .andDo(print());
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                    .writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Long extractIdFromLocation(String location) {
        String id = location.substring(location.lastIndexOf("/") + 1);
        return Long.parseLong(id);
    }
}